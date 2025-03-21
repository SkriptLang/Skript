package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Feature;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.util.SkriptQueue;

import java.util.Iterator;

@Name("Queue (Experimental)")
@Description("""
	Requires the <code>using queues</code> experimental feature flag to be enabled.
	
	Creates a new queue.
	A queue is a set of elements that can have things removed from the start and added to the end.
	
	Any value can be added to a queue. Adding a non-existent value (e.g. `{variable that isn't set}`) will have no effect.
	This means that removing an element from the queue will always return a value <i>unless the queue is empty</i>.
	
	Requesting an element from a queue (e.g. `the 1st element of {queue}`) also removes it from the queue.""")
@Examples({
	"""
		set {queue} to a new queue
		add "hello" and "there" to {queue}
		broadcast the first element of {queue} # hello
		broadcast the first element of {queue} # there
		# queue is now empty""",
	"""
		set {queue} to a new queue of "hello" and "there"
		broadcast the last element of {queue} # removes 'there'
		add "world" to {queue}
		broadcast the first 2 elements of {queue} # removes 'hello', 'world'"""
})
@Since("2.10 (experimental)")
public class ExprQueue extends SimpleExpression<SkriptQueue> {

	static {
		Skript.registerExpression(ExprQueue.class, SkriptQueue.class, ExpressionType.COMBINED,
			"[a] [new] queue [(of|with) %-objects%]");
	}

	private @Nullable Expression<?> contents;

	@Override
	public boolean init(Expression<?>[] expressions, int pattern, Kleenean delayed, ParseResult result) {
		if (!this.getParser().hasExperiment(Feature.QUEUES))
			return false;
		if (expressions[0] != null)
			this.contents = LiteralUtils.defendExpression(expressions[0]);
		return contents == null || LiteralUtils.canInitSafely(contents);
	}

	@Override
	protected SkriptQueue @Nullable [] get(Event event) {
		SkriptQueue queue = new SkriptQueue();
		SkriptQueue[] result = new SkriptQueue[]{queue};
		if (contents == null)
			return result;
		Iterator<?> iterator = contents.iterator(event);
		if (iterator == null)
			return result;
		while (iterator.hasNext())
			queue.add(iterator.next());
		return result;
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends SkriptQueue> getReturnType() {
		return SkriptQueue.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (contents == null)
			return "a queue";
		return "a queue of " + contents.toString(event, debug);
	}

}
