package ch.njol.skript.sections;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.ContainerExpression;
import ch.njol.skript.registrations.Feature;
import ch.njol.skript.util.Container;
import ch.njol.skript.util.Container.ContainerType;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import com.google.common.collect.PeekingIterator;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.*;

@Name("Loop")
@Description({
	"Loop sections repeat their code with multiple values.",
	"",
	"A loop will loop through all elements of the given expression, e.g. all players, worlds, items, etc. " +
		"The conditions & effects inside the loop will be executed for every of those elements, " +
		"which can be accessed with ‘loop-<what>’, e.g. <code>send \"hello\" to loop-player</code>. " +
		"When a condition inside a loop is not fulfilled the loop will start over with the next element of the loop. " +
		"You can however use <code>stop loop</code> to exit the loop completely and resume code execution after the end of the loop.",
	"",
	"<b>Loopable Values</b>",
	"All <a href=\"/expressions.html\">expressions</a> that represent more than one value, e.g. ‘all players’, ‘worlds’, " +
		"etc., as well as list variables, can be looped. You can also use a list of expressions, e.g. <code>loop the victim " +
		"and the attacker</code>, to execute the same code for only a few values.",
	"",
	"<b>List Variables</b>",
	"When looping list variables, you can also use <code>loop-index</code> in addition to <code>loop-value</code> inside " +
		"the loop. <code>loop-value</code> is the value of the currently looped variable, and <code>loop-index</code> " +
		"is the last part of the variable's name (the part where the list variable has its asterisk *)."
})
@Examples({
	"loop all players:",
		"\tsend \"Hello %loop-player%!\" to loop-player",
	"",
	"loop items in player's inventory:",
		"\tif loop-item is dirt:",
			"\t\tset loop-item to air",
	"",
	"loop 10 times:",
		"\tsend title \"%11 - loop-value%\" and subtitle \"seconds left until the game begins\" to player for 1 second # 10, 9, 8 etc.",
		"\twait 1 second",
	"",
	"loop {Coins::*}:",
		"\tset {Coins::%loop-index%} to loop-value + 5 # Same as \"add 5 to {Coins::%loop-index%}\" where loop-index is the uuid of " +
		"the player and loop-value is the number of coins for the player",
	"",
	"loop shuffled (integers between 0 and 8):",
		"\tif all:",
			"\t\tprevious loop-value = 1",
			"\t\tloop-value = 4",
			"\t\tnext loop-value = 8",
		"\tthen:",
			"\t\t kill all players"
})
@Since("1.0")
public class SecLoop extends LoopSection {

	static {
		Skript.registerSection(SecLoop.class, "loop %objects%");
	}

	protected @UnknownNullability Expression<?> expression;

	private final transient Map<Event, Object> current = new WeakHashMap<>();
	private final transient Map<Event, Iterator<?>> iteratorMap = new WeakHashMap<>();
	private final transient Map<Event, Object> previous = new WeakHashMap<>();

	protected @Nullable TriggerItem actualNext;
	private boolean guaranteedToLoop;
	private Object nextValue = null;
	private boolean loopPeeking;
	protected boolean iterableSingle;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs,
						int matchedPattern,
						Kleenean isDelayed,
						ParseResult parseResult,
						SectionNode sectionNode,
						List<TriggerItem> triggerItems) {
		this.expression = LiteralUtils.defendExpression(exprs[0]);
		if (!LiteralUtils.canInitSafely(expression)) {
			Skript.error("Can't understand this loop: '" + parseResult.expr.substring(5) + "'");
			return false;
		}

		if (Container.class.isAssignableFrom(expression.getReturnType())) {
			ContainerType type = expression.getReturnType().getAnnotation(ContainerType.class);
			if (type == null)
				throw new SkriptAPIException(expression.getReturnType().getName() + " implements Container but is missing the required @ContainerType annotation");
			this.expression = new ContainerExpression((Expression<? extends Container<?>>) expression, type.value());
		}

		if (this.getParser().hasExperiment(Feature.QUEUES) // Todo: change this if other iterable things are added
			&& expression.isSingle()
			&& (expression instanceof Variable<?> || Iterable.class.isAssignableFrom(expression.getReturnType()))) {
			// Some expressions return one thing but are potentially iterable anyway, e.g. queues
			this.iterableSingle = true;
		} else if (expression.isSingle()) {
			Skript.error("Can't loop '" + expression + "' because it's only a single value");
			return false;
		}
		loopPeeking = exprs[0].supportsLoopPeeking();

		guaranteedToLoop = guaranteedToLoop(expression);
		loadOptionalCode(sectionNode);
		super.setNext(this);

		return true;
	}

	@Override
	protected @Nullable TriggerItem walk(Event event) {
		Iterator<?> iter = iteratorMap.get(event);
		if (iter == null) {
			if (iterableSingle) {
				Object value = expression.getSingle(event);
				if (value instanceof Iterable<?> iterable) {
					iter = iterable.iterator();
					// Guaranteed to be ordered so we try it first
				} else if (value instanceof Container<?> container) {
					iter = container.containerIterator();
				} else {
					iter = Collections.singleton(value).iterator();
				}
			} else {
				iter = expression instanceof Variable<?> variable ? variable.variablesIterator(event) :
					expression.iterator(event);
				if (iter != null && iter.hasNext()) {
					iteratorMap.put(event, iter);
				} else {
					iter = null;
				}
			}
		}

		if (iter == null || (!iter.hasNext() && nextValue == null)) {
			exit(event);
			debug(event, false);
			return actualNext;
		} else {
			previous.put(event, current.get(event));
			if (nextValue != null) {
				this.store(event, nextValue);
				nextValue = null;
			} else if (iter.hasNext()) {
				this.store(event, iter.next());
			}
			return walk(event, true);
		}
	}

	protected void store(Event event, Object next) {
		this.current.put(event, next);
		this.currentLoopCounter.put(event, (currentLoopCounter.getOrDefault(event, 0L)) + 1);
	}

	@Override
	public @Nullable ExecutionIntent executionIntent() {
		return guaranteedToLoop ? triggerExecutionIntent() : null;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "loop " + expression.toString(event, debug);
	}

	public @Nullable Object getCurrent(Event event) {
		return current.get(event);
	}

	public @Nullable Object getNext(Event event) {
		if (!loopPeeking)
			return null;
		Iterator<?> iter = iteratorMap.get(event);
		if (iter == null || !iter.hasNext())
			return null;
		if (iter instanceof PeekingIterator<?> peekingIterator)
			return peekingIterator.peek();
		nextValue = iter.next();
		return nextValue;
	}

	public @Nullable Object getPrevious(Event event) {
		return previous.get(event);
	}

	public Expression<?> getLoopedExpression() {
		return expression;
	}

	@Override
	public SecLoop setNext(@Nullable TriggerItem next) {
		actualNext = next;
		return this;
	}

	@Nullable
	@Override
	public TriggerItem getActualNext() {
		return actualNext;
	}

	@Override
	public void exit(Event event) {
		current.remove(event);
		iteratorMap.remove(event);
		previous.remove(event);
		super.exit(event);
	}

	private static boolean guaranteedToLoop(Expression<?> expression) {
		// If the expression is a literal, it's guaranteed to loop if it has at least one value
		if (expression instanceof Literal<?> literal)
			return literal.getAll().length > 0;

		// If the expression isn't a list, then we can't guarantee that it will loop
		if (!(expression instanceof ExpressionList<?> list))
			return false;

		// If the list is an OR list (a, b or c), then it's guaranteed to loop iff all its values are guaranteed to loop
		if (!list.getAnd()) {
			for (Expression<?> expr : list.getExpressions()) {
				if (!guaranteedToLoop(expr))
					return false;
			}
			return true;
		}

		// If the list is an AND list (a, b and c), then it's guaranteed to loop if any of its values are guaranteed to loop
		for (Expression<?> expr : list.getExpressions()) {
			if (guaranteedToLoop(expr))
				return true;
		}

		// Otherwise, we can't guarantee that it will loop
		return false;
	}

	public boolean supportsPeeking() {
		return loopPeeking;
	}

	public Expression<?> getExpression() {
		return expression;
	}

}
