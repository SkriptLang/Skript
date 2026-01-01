package ch.njol.skript.lang;

import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * An asynchronous expression, that is to say, an expression the value of which might not be immediately available.
 * When one of the <code>getX</code> methods of {@link Expression} is called on an asynchronous expression, it will
 * either return the result value as normal or yield by throwing a {@link HandlerYieldException}. The yield exception
 * allows callers to register a callback that will be run only when the asynchronous exception has finished its computation,
 * such that calling one of the <code>getX</code> methods from the callback will work as normal without yielding.
 * <p>Make sure to set {@link ParserInstance#getHasDelayBefore()} to  {@link Kleenean#TRUE} in the {@code init} method.
 * <p>
 * See {@link SimpleAsyncExpression} for an example of how to track whether to return the computed result or create
 * a new computation.
 * <p>This is experimental because of issues related to the handling of asynchronous expressions. For example, an effect
 * that contains multiple expressions of which one yields may invoke some non-async expressions more than once, or async
 * expressions an arbitrary number of times. To give an example, <code>broadcast foo() and %some async expression%</code> may run
 * <code>foo()</code> twice.
 * @see SimpleAsyncExpression
 * */
@ApiStatus.Experimental
public interface AsyncExpression<T> extends Expression<T> {
	/**
	 * The execution context of an asynchronous expression. May be used by implementations to record whether the current
	 * execution is supposed to start a new task or return the completed result of the previously started task.
	 * */
	interface Context<T> {
		UUID id();
	}

	/**
	 * @return The current context, creating one if it does not exist.
	 * */
	Context<T> currentContext();
	/**
	 * Resets the context, such that a later call to {@link AsyncExpression#currentContext()} will have a different identifier
	 * and {@link AsyncExpression#computeAll(Event)} and related methods will start new tasks.
	 * */
	void resetContext();

	/**
	 * Starts a new computation for {@link Expression#getAll(Event)}, or returns the future corresponding to the previous
	 * invocation's future if it is completed. In this way, return values of this method should alternate between
	 * completed and incomplete futures. It may be assumed that the {@link CompletableFuture} returned by a previous
	 * invocation of this method will be completed when this method is called.
	 * @see Expression#getAll(Event)
	 * */
	CompletableFuture<T[]> computeAll(Event event);
	/**
	 * Starts a new computation for {@link Expression#getArray(Event)}, or returns the future corresponding to the previous
	 * invocation's future if it is completed. In this way, return values of this method should alternate between
	 * completed and incomplete futures. It may be assumed that the {@link CompletableFuture} returned by a previous
	 * invocation of this method will be completed when this method is called.
	 * @see Expression#getArray(Event)
	 * */
	CompletableFuture<T[]> computeArray(Event event);
	/**
	 * Starts a new computation for {@link Expression#getSingle(Event)}, or returns the future corresponding to the previous
	 * invocation's future if it is completed. In this way, return values of this method should alternate between
	 * completed and incomplete futures. It may be assumed that the {@link CompletableFuture} returned by a previous
	 * invocation of this method will be completed when this method is called.
	 * @see Expression#getSingle(Event)
	 * */
	CompletableFuture<T> computeSingle(Event event);

	private  <Q> Q valueOrYield(final Future<Q> future, final Consumer<HandlerYieldException> yieldRegistrar) {
		return switch (future.state()) {
			case RUNNING -> {
				final var yield = new HandlerYieldException(this);
				yieldRegistrar.accept(yield);
				throw yield;
			}
			case SUCCESS -> future.resultNow();
			case CANCELLED -> throw new CancellationException();
			case FAILED -> throw new RuntimeException(future.exceptionNow());
		};
	}

	private <Q> Q createYield(final CompletableFuture<Q> future) {
		return valueOrYield(future, yield -> future.thenRun(yield::resolve));
	}


	@Override
	default T[] getAll(Event event) {
		return createYield(computeAll(event));
	}

	@Override
	default T[] getArray(Event event) {
		return createYield(computeArray(event));
	}

	@Override
	@Nullable
	default T getSingle(Event event) {
		return createYield(computeSingle(event));
	}
}
