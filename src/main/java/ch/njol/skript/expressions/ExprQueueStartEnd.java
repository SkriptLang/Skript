package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.ExperimentalSyntax;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Feature;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.experiment.ExperimentSet;
import org.skriptlang.skript.lang.util.SkriptQueue;

import java.util.Arrays;

@Name("Queue Start/End (Experimental)")
@Description("""
	Requires the <code>using queues</code> experimental feature flag to be enabled.
	
	The first or last element in a queue. Asking for this does <b>not</b> remove the element from the queue.
	
	This is designed for use with the <code>add</code> changer: to add or remove elements from the start or the end of the queue.
	""")
@Examples("""
	set {queue} to a new queue
	add "hello" to {queue}
	add "foo" to the start of {queue}
	broadcast the first element of {queue} # foo
	broadcast the first element of {queue} # hello
	# queue is now empty""")
@Since("2.10 (experimental)")
public class ExprQueueStartEnd extends SimplePropertyExpression<SkriptQueue, Object> implements ExperimentalSyntax {

	static {
		register(ExprQueueStartEnd.class, Object.class, "(:start|end)", "queue");
	}

	private boolean start;

	@Override
	public boolean init(Expression<?>[] expressions, int pattern, Kleenean delayed, ParseResult result) {
		this.start = result.hasTag("start");
		return super.init(expressions, pattern, delayed, result);
	}

	@Override
	public boolean isSatisfiedBy(ExperimentSet experimentSet) {
		return experimentSet.hasExperiment(Feature.QUEUES);
	}

	@Override
	public @Nullable Object convert(SkriptQueue from) {
		return start ? from.peekFirst() : from.peekLast();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(Changer.ChangeMode mode) {
		return switch (mode) {
			case ADD, REMOVE, SET -> new Class<?>[] {Object.class};
			case DELETE -> new Class[0];
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, Changer.ChangeMode mode) {
		SkriptQueue queue = this.getExpr().getSingle(event);
		if (queue == null)
			return;
		if (start) {
			switch (mode) {
				case DELETE -> queue.removeFirst();
				case ADD -> queue.addAll(0, Arrays.asList(delta));
				case SET -> {
					assert delta != null && delta.length > 0;
					queue.removeFirst();
					queue.addFirst(delta[0]);
				}
				case REMOVE -> {
					for (Object object : delta)
						queue.removeFirstOccurrence(object);
				}
			}
		} else {
			switch (mode) {
				case DELETE -> queue.removeLast();
				case ADD -> queue.addAll(Arrays.asList(delta));
				case SET -> {
					assert delta != null && delta.length > 0;
					queue.removeLast();
					queue.addLast(delta[0]);
				}
				case REMOVE -> {
					for (Object object : delta)
						queue.removeLastOccurrence(object);
				}
			}
		}
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<?> getReturnType() {
		return Object.class;
	}

	@Override
	protected String getPropertyName() {
		return start ? "start" : "end";
	}

}
