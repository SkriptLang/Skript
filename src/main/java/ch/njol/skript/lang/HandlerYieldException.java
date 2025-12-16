package ch.njol.skript.lang;

import ch.njol.skript.Skript;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.lang.function.ScriptFunction;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Internal control-flow exception used by the Skript interpreter to signal that
 * the current return handler (for example, a function call that is expected to
 * produce a return value) has yielded execution and will resume later.
 * <p>
 * Yielding can happen when the script encounters an instruction that schedules
 * a continuation instead of proceeding synchronously, such as the "wait"
 * effect ({@link Delay}). In such cases, the code expecting a return value
 * (e.g. a {@link ScriptFunction}) cannot proceed immediately. Rather than blocking
 * the server thread, the interpreter throws this exception to unwind the Java call
 * stack. The call site may catch this exception to register callbacks that should
 * run when the delayed part finishes, effectively resuming the computation.
 * <p>
 * Instances of this exception carry a thread-safe set of {@link Runnable}
 * callbacks. The delaying code is responsible for invoking these callbacks once
 * the continuation is ready. See {@link Delay} for an example.
 * <p>
 * Also included within an instance is the {@link SyntaxElement} that caused the yield,
 * which is used to prevent yield loops.
 * <p>
 * This type is not intended for public consumption; it is an implementation
 * detail of Skript's interpreter. It extends {@link RuntimeException} so that
 * it can be thrown across APIs without boilerplate and caught only where the
 * interpreter knows how to resume execution.
 */
public class HandlerYieldException extends RuntimeException {
	private final Set<Runnable> resumeCallbacks = Collections.synchronizedSet(new LinkedHashSet<>());
	private final SyntaxElement sourceElement;

	public HandlerYieldException(final SyntaxElement source) {
		this.sourceElement = source;
	}

	private boolean resolved = false;

	/**
	 * Registers a listener that will be executed when the delayed execution
	 * resumes. Listeners are run by the delaying code (for example, on the main
	 * server thread by {@link Delay} after the scheduled
	 * wait finishes).
	 * <p>
	 * Callbacks are completed by the delaying code in the order they were added.
	 *
	 * @param task the callback to run upon resume; must be non-null
	 */
	public void addResumeCallback(final Runnable task) {
		this.resumeCallbacks.add(task);
	}

	/**
	 * Returns a snapshot copy of the currently registered listeners.
	 *
	 * @return a new {@link LinkedHashSet} containing the registered tasks.
	 */
	protected Set<Runnable> getResumeCallbacks() {
		return new LinkedHashSet<>(resumeCallbacks);
	}

	/**
	 * Resolves this yield by executing all resume callbacks in order.
	 * <p>
	 * If one of the callbacks itself yields, then all not-yet-executed callbacks will be registered
	 * for that yield instead and will execute when that inner yield is resolved.
	 * */
	public void resolve() {
		final Set<Runnable> callbacks = getResumeCallbacks();
		for (Iterator<Runnable> iterator = callbacks.iterator(); iterator.hasNext(); ) {
			try {
				iterator.next().run();
			} catch (final HandlerYieldException innerYield) {
				if (innerYield.sourceElement == this.sourceElement) throw Skript.exception(this,
					"Yield contract broken!", "The syntax element '" + sourceElement + "' yielded when it should have had a result.");


				while (iterator.hasNext()) innerYield.addResumeCallback(iterator.next());
			}
		}
		resolved = true;
	}

	/**
	 * @return True if this yield has been resolved with {@link HandlerYieldException#resolve()}
	 * */
	public boolean isResolved() {
		return resolved;
	}
}
