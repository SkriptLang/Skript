package ch.njol.skript.test.runner;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SimpleAsyncExpression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.LockSupport;

// Test class for async expressions.
public class ExprStall extends SimpleAsyncExpression<Object> {
	static {
		Skript.registerExpression(ExprStall.class, Object.class, ExpressionType.SIMPLE, "%objects% stalled for %timespan%");
	}

	private Expression<Object> objects;
	private Expression<Timespan> delay;

	@Override
	protected CompletableFuture<Object[]> compute(Event event) {
		final Timespan timespan = delay.getSingle(event);
		if (timespan == null) return CompletableFuture.completedFuture(objects.getArray(event));
		final long nanos = timespan.getDuration().toNanos();
		return CompletableFuture.runAsync(() -> LockSupport.parkNanos(nanos))
			.thenApplyAsync(v -> objects.getArray(event), getMainThreadExecutor());
	}

	@Override
	public boolean isSingle() {
		return objects.isSingle();
	}

	@Override
	public Class<?> getReturnType() {
		return Object.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return objects.toString(event, debug) + " stalled for " + delay.toString(event, debug);
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		this.objects = (Expression<Object>) expressions[0];
		this.delay = (Expression<Timespan>) expressions[1];

		getParser().setHasDelayBefore(Kleenean.TRUE);
		return LiteralUtils.canInitSafely(objects, delay);
	}
}
