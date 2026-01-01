package ch.njol.skript.lang;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.SimpleAsyncExpression.SimpleContext.Complete;
import ch.njol.skript.lang.SimpleAsyncExpression.SimpleContext.Running;
import ch.njol.skript.lang.util.SimpleExpression;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

/**
 * An {@link AsyncExpression} equivalent to {@link SimpleExpression}.
 * You should usually extend this class to make an asynchronous expression.
 * <p>When running code asynchronously, you may find {@link SimpleAsyncExpression#getMainThreadExecutor()} to be useful.
 * Don't accidentally run Skript code outside the server thread!
 * <p>This is experimental because of issues related to the handling of asynchronous expressions. For example, an effect
 * that contains multiple expressions of which one yields may invoke some non-async expressions more than once, or async
 * expressions an arbitrary number of times. To give an example, <code>broadcast foo() and %some async expression%</code> may run
 * <code>foo()</code> twice.
 * */
@ApiStatus.Experimental
public abstract class SimpleAsyncExpression<T> extends SimpleExpression<T> implements AsyncExpression<T> {
	protected abstract CompletableFuture<T[]> compute(final Event event);

	/*
	* A context that keeps track of the async expression state.
	* The state will be UNBOUND iff one of the following is true:
	*   1. get() has never been called on this thread for this expression instance, or
	*   2. get() has been called and the call returned a completed result
	*        (it is necessary to reset the state so that things like loops correctly re-evaluate the expression)
	* The state will be RUNNING iff get() has been called but the relevant CompletableFuture has not finished.
	* The state will be COMPLETE iff get() has been called, the relevant CompletableFuture has finished,
	*     and nobody has yet claimed the computed value with a subsequent call to get()
	*     (which would change the state to UNBOUND).
	* */
	public static class SimpleContext<T> implements Context<T> {
		private final UUID uuid = UUID.randomUUID();
		private State<T> state = new Unbound<>();

		sealed interface State<T> permits Unbound, Running, Complete {}
		static final class Unbound<T> implements State<T> {}
		record Running<T>(CompletableFuture<T[]> future) implements State<T> {}
		record Complete<T>(CompletableFuture<T[]> future) implements State<T> {
			public T[] resultNow() {
				return future().resultNow();
			}
		}

		public SimpleContext() {
		}

		static <T> SimpleContext<T> start(final CompletableFuture<T[]> future) {
			final var ctx = new SimpleContext<T>();
			ctx.state = new Running<>(future);
			future.whenComplete((values, error) -> ctx.state = new Complete<>(future));
			return ctx;
		}

		@Override
		public UUID id() {
			return uuid;
		}

		State<T> getState() {
			return state;
		}
	}

	private final ThreadLocal<SimpleContext<T>> context = ThreadLocal.withInitial(SimpleContext::new);

	/**
	 * A version of {@link SimpleAsyncExpression#compute(Event)} that forces further child completion stages of the future
	 * to run on the bukkit server thread.
	 * @return The completable future from {@link SimpleAsyncExpression#compute(Event)}, but such that the last completion
	 * stage runs on the server thread, so any further calls to {@link CompletableFuture#thenRun(Runnable)} will run on the server thread.
	 * */
	protected CompletableFuture<T[]> paperGuardedCompute(final Event event) {
		return compute(event).thenApplyAsync(Function.identity(),
			getMainThreadExecutor());
	}

	public static @NotNull Executor getMainThreadExecutor() {
		return Bukkit.getScheduler().getMainThreadExecutor(Skript.getInstance());
	}

	@Override
	public SimpleContext<T> currentContext() {
		return context.get();
	}

	@Override
	protected T @Nullable [] get(Event event) {
		final SimpleContext<T> ctx = currentContext();
		// Since yields are resolved only after the future completes,
		// it is not possible for a second get() call to happen before the future has completed.
		assert !(ctx.getState() instanceof Running<T>): "duplicate invocation";
		if (ctx.getState() instanceof Complete<T> complete) {
			final T[] result = complete.resultNow();
			resetContext();
			return result;
		}
		final HandlerYieldException yield = new HandlerYieldException(this);
		final CompletableFuture<T[]> future = paperGuardedCompute(event);
		future.thenRun(yield::resolve);
		context.set(SimpleContext.start(future));
		throw yield;
	}

	private CompletableFuture<T[]> getOrCreateComputation(final Event event) {
		return switch (context.get().getState()) {
			case SimpleContext.Complete<T> complete -> complete.future();
			case Running<T> running -> running.future();
			case SimpleContext.Unbound<T> ignored -> {
				final CompletableFuture<T[]> future = paperGuardedCompute(event);
				context.set(SimpleContext.start(future));
				yield future;
			}
		};
	}

	@Override
	public void resetContext() {
		context.remove();
	}

	// These methods just forward to the `get` method through SimpleExpression's `getX` methods, but wait for the
	// computation to be complete so that a result is guaranteed to be available. These are, in practice, never called
	// because the synchronous equivalents for SimpleExpression call get() directly.
	@Override
	public CompletableFuture<T[]> computeAll(Event event) {
		return getOrCreateComputation(event).thenApply(v -> getAll(event));
	}

	@Override
	public CompletableFuture<T[]> computeArray(Event event) {
		return getOrCreateComputation(event).thenApply(v -> getArray(event));
	}

	@Override
	public CompletableFuture<T> computeSingle(Event event) {
		return getOrCreateComputation(event).thenApply(v -> getSingle(event));
	}
}
