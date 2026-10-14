package org.skriptlang.skript.util;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The outcome of an operation which either produces a value or explains why it could not.
 * <p>
 * A {@link Success} carries the value and may carry an advisory message; a {@link Failure} carries
 * only the reason it failed. Which of the two it is answers whether the operation succeeded, so an
 * operation whose value is legitimately absent, such as one returning nothing at all, is still a
 * success and is still told apart from a failure.
 * </p>
 * <p>
 * New code should match on the two cases, which lets the compiler see that a failure has no value:
 * <pre>{@code
 * switch (result) {
 *     case Success<Thing>(Thing thing, String warning) -> use(thing);
 *     case Failure<Thing>(String error) -> report(error);
 * }
 * }</pre>
 * Code which only cares whether there is a value can use {@link #isSuccess()} together with
 * {@link #value()} and {@link #message()} instead.
 * </p>
 *
 * @param <T> The type of the value a successful outcome carries.
 */
public sealed interface Result<T> {

	/**
	 * @param value The value produced.
	 * @param <T>   The type of the value.
	 * @return A successful result carrying {@code value}.
	 */
	@Contract(value = "-> new", pure = true)
	static <T> @NotNull Result<T> success(@Nullable T value) {
		return new Success<>(value, null);
	}

	/**
	 * @param value   The value produced.
	 * @param warning Something the caller should be told although the operation succeeded, or null.
	 * @param <T>     The type of the value.
	 * @return A successful result carrying {@code value} and {@code warning}.
	 */
	@Contract(value = "_, _ -> new", pure = true)
	static <T> @NotNull Result<T> success(@Nullable T value, @Nullable String warning) {
		return new Success<>(value, warning);
	}

	/**
	 * @param error Why the operation did not succeed.
	 * @param <T>   The type of the value a successful outcome would have carried.
	 * @return A failed result carrying {@code error}.
	 */
	@Contract(value = "_ -> new", pure = true)
	static <T> @NotNull Result<T> failure(@NotNull String error) {
		return new Failure<>(error);
	}

	/**
	 * @return Whether the operation succeeded. A successful result may still carry a message, and
	 * 	may carry no value.
	 */
	boolean isSuccess();

	/**
	 * @return The value produced, or null if the operation failed or produced no value. Use
	 * 	{@link #isSuccess()} to tell those apart.
	 */
	@Nullable T value();

	/**
	 * @return The reason the operation failed, if it did, or an advisory message about a successful
	 * 	one, or null if there is nothing to report. A failure always has one.
	 */
	@Nullable String message();

	/**
	 * An operation which produced a value.
	 *
	 * @param value   The value produced, which may be null if the operation produces none.
	 * @param warning Something the caller should be told although the operation succeeded, or null.
	 * @param <T>     The type of the value.
	 */
	record Success<T>(@Nullable T value, @Nullable String warning) implements Result<T> {

		@Override
		public boolean isSuccess() {
			return true;
		}

		@Override
		public @Nullable String message() {
			return warning;
		}

	}

	/**
	 * An operation which did not produce a value, and why.
	 *
	 * @param error Why the operation did not succeed.
	 * @param <T>   The type of the value a successful outcome would have carried.
	 */
	record Failure<T>(@NotNull String error) implements Result<T> {

		public Failure {
			Preconditions.checkNotNull(error, "a failure must say why it failed");
		}

		@Override
		public boolean isSuccess() {
			return false;
		}

		@Override
		public @Nullable T value() {
			return null;
		}

		@Override
		public @NotNull String message() {
			return error;
		}

	}

}
