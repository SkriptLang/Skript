package org.skriptlang.skript.util;

import ch.njol.skript.lang.Expression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * A process that can be executed, which may return its result.
 * This is an abstraction for general triggers (runnables, tasks, functions, etc.)
 * without any Bukkit dependency.
 *
 * @param <Caller> The source type of this process (e.g. Event, CommandSender, etc.)
 * @param <Result> The return type of this process.
 */
public interface Executable<Caller, Result> {

	/**
	 * Executes this with the given argument values.
	 * <p>
	 * The values are positional and carry no structure, so anything which cares about which of
	 * its inputs an argument belongs to should implement {@link #bind(Expression[])} instead and
	 * be called through that.
	 * </p>
	 *
	 * @param caller    The source to execute with.
	 * @param arguments The argument values.
	 * @return The result.
	 */
	Result execute(Caller caller, Object... arguments);

	/**
	 * Binds a set of argument expressions to this, so that it can be executed with them
	 * repeatedly.
	 * <p>
	 * This is how something which has its own idea of what its arguments mean, such as a function
	 * with parameters, decides for itself how the arguments it was given are used. An executable
	 * with no such need does not implement this, and is called through
	 * {@link #execute(Object, Object...)} with the evaluated values instead.
	 * </p>
	 *
	 * @param arguments The argument expressions, as collected when the caller was parsed.
	 * @return Something which executes this with those arguments, or null if this does not bind
	 * arguments itself. Note that the arguments being unacceptable is reported by the returned
	 * {@link BoundExecutable} rather than here, since whether they are may depend on their values.
	 */
	default @Nullable Executable.BoundExecutable<Caller, Result> bind(Expression<?>... arguments) {
		return null;
	}

	/**
	 * Executes {@code bound}, reporting why it would not run if it did not.
	 * <p>
	 * A bound executable knows why it would not accept the arguments it was bound to, but not
	 * where the call to it was written, so the reason is reported through {@code onRejection}
	 * rather than logged.
	 * </p>
	 *
	 * @param bound       The bound executable.
	 * @param caller      The source to execute with.
	 * @param onRejection Called with the reason if it did not run.
	 * @param <Caller>    The source type of the executable.
	 * @param <Result>    The return type of the executable.
	 * @return The result, or null if it did not run.
	 */
	static <Caller, Result> @Nullable Result run(
		BoundExecutable<Caller, Result> bound, Caller caller, Consumer<String> onRejection
	) {
		Result result = bound.execute(caller);
		if (result != null) {
			return result;
		}

		onRejection.accept(bound.rejection());
		return null;
	}

	/**
	 * An executable which has had a set of arguments bound to it.
	 *
	 * @param <Caller> The source type of the executable.
	 * @param <Result> The return type of the executable.
	 */
	@FunctionalInterface
	interface BoundExecutable<Caller, Result> {

		/**
		 * @param caller The source to execute with.
		 * @return The result, or null if this did not run, because the bound arguments turned out
		 * 	not to be acceptable or execution failed. An executable which ran but has no result
		 * 	returns an empty array or an equivalent empty value rather than null, so that callers
		 * 	can tell the two apart.
		 */
		@Nullable Result execute(Caller caller);

		/**
		 * Why {@link #execute(Object)} did not run, so that the caller can report it along with
		 * where the call was written.
		 * <p>
		 * This is only asked once {@link #execute(Object)} has returned null, so an implementation
		 * which cannot tell whether the arguments fit until they have been evaluated may answer
		 * with what it learnt while executing. It is returned rather than logged because an
		 * executable has no way of knowing which log, if any, is listening when it runs.
		 * </p>
		 *
		 * @return The reason this did not run. Never null, since it is only asked when there is
		 * 	one, and an implementation which names itself here is what keeps the reported message
		 * 	specific.
		 */
		default @NotNull String rejection() {
			return "The given arguments are not acceptable.";
		}

	}

}
