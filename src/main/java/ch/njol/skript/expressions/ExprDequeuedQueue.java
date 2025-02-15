package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.ExperimentalSyntax;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Feature;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.util.SkriptQueue;

@Name("De-queue Queue (Experimental)")
@Description("""
	Requires the <code>using queues</code> experimental feature flag to be enabled.
	
	Unrolls a queue into a regular list of values, which can be stored in a list variable.
	The order of the list will be the same as the order of the elements in the queue.
	If a list variable is set to this, it will use numerical indices.
	The original queue will not be changed.""")
@Examples({
	"""
		set {queue} to a new queue
		add "hello" and "there" to {queue}
		set {list::*} to dequeued {queue}"""
})
@Since("2.10 (experimental)")
public class ExprDequeuedQueue extends SimpleExpression<Object> implements ExperimentalSyntax {

	static {
		Skript.registerExpression(ExprDequeuedQueue.class, Object.class, ExpressionType.COMBINED,
			"(de|un)queued %queue%", "unrolled %queue%");
	}

	private Expression<SkriptQueue> queue;

	@Override
	public boolean init(Expression<?>[] expressions, int pattern, Kleenean delayed, ParseResult result) {
		//noinspection unchecked
		this.queue = (Expression<SkriptQueue>) expressions[0];
		return true;
	}

	@Override
	public Feature requiredExperiment() {
		return Feature.QUEUES;
	}

	@Override
	protected Object @Nullable [] get(Event event) {
		SkriptQueue queue = this.queue.getSingle(event);
		if (queue == null)
			return null;
		return queue.toArray();
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<Object> getReturnType() {
		return Object.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "dequeued " + queue.toString(event, debug);
	}

}
