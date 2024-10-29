package ch.njol.skript.lang;

import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * Represents an element that can print details involving an event.
 */
public interface Debuggable {

	/**
	 * Returns a string representation of this object.
	 *
	 * @param event The event to get information from. This is always null if debug == false.
	 * @param debug If true this should print more information, if false this should print what is shown to the end user
	 * @return A string representation of this object.
	 */
	String toString(@Nullable Event event, boolean debug);

	/**
	 * Should return <tt>{@link #toString(Event, boolean) toString}(null, false)</tt>
	 */
	@Override
	String toString();

	/**
	 * Formats a string with the given (optional) arguments, for constructing a {@link #toString(Event, boolean)}
	 * representation.
	 *
	 * @param string The string to format.
	 * @param args The args to replace.
	 * @return The formatted string.
	 * @see String#format(String, Object...)
	 */
	default String formattedToString(@NotNull String string, Object... args) {
		return string.formatted(args).replaceAll("  +", " ");
	}

	/**
	 * Represents an optional value in a {@link #toString(Event, boolean)} method.
	 * <ul>
	 *     <li>If {@code value} is not null, returns a class which returns the result of the function {@code ifNotNull}.
	 *     This function applies a not-null {@code value} to get a string, which removes null warnings.</li>
	 *     <li>If {@code value} is null, returns a class which returns an empty string.</li>
	 * </ul>
	 *
	 * @param value The potentially null value.
	 * @param ifNotNull The function to apply if {@code value} is not null.
	 * @return A class which gives the string representation of the value, or an empty string if it is null.
	 * @param <T> The type of the value.
	 */
	default <T> OptionalDebugParameter<T> optional(
		@Nullable T value,
		@NotNull Function<@NotNull T, String> ifNotNull
	) {
		return optional(value, ifNotNull, "");
	}

	/**
	 * Represents an optional value in a {@link #toString(Event, boolean)} method.
	 * <ul>
	 *     <li>If {@code value} is not null, returns a class which returns the result of the function {@code ifNotNull}.
	 *     This function applies a not-null {@code value} to get a string, which removes null warnings.</li>
	 *     <li>If {@code value} is null, returns a class which returns {@code ifNull}.</li>
	 * </ul>
	 *
	 * @param value The potentially null value.
	 * @param ifNotNull The function to apply if {@code value} is not null.
	 * @param ifNull The string to return if {@code value} is null.
	 * @return A class which gives the string representation of the value, or {@code ifNull} if it is null.
	 * @param <T> The type of the value.
	 */
	default <T> OptionalDebugParameter<T> optional(
		@Nullable T value,
		@NotNull Function<@NotNull T, String> ifNotNull,
		@NotNull String ifNull
	) {
		return new OptionalDebugParameter<>(value, ifNotNull, ifNull);
	}

	final class OptionalDebugParameter<T> {

		private final @Nullable T value;
		private final @NotNull Function<@NotNull T, String> ifNotNull;
		private final @NotNull String ifNull;

		public OptionalDebugParameter(
			@Nullable T value,
			@NotNull Function<@NotNull T, String> ifNotNull,
			@NotNull String ifNull
		) {
			this.value = value;
			this.ifNotNull = ifNotNull;
			this.ifNull = ifNull;
		}

		@Override
		public String toString() {
			return value != null ? ifNotNull.apply(value) : ifNull;
		}

	}

}
