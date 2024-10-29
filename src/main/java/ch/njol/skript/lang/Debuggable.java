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

	default String formattedToString(@NotNull String string, Object... args) {
		return string.formatted(args).replaceAll("  +", " ");
	}

	default <T> OptionalDebugParameter<T> optional(T value, Function<@NotNull T, String> ifNotNull) {
		return optional(value, ifNotNull, "");
	}

	default <T> OptionalDebugParameter<T> optional(T value, Function<@NotNull T, String> ifNotNull, String ifNull) {
		return new OptionalDebugParameter<>(value, ifNotNull, ifNull);
	}

	final class OptionalDebugParameter<T> {

		private final T value;
		private final Function<@NotNull T, String> ifNotNull;
		private final String ifNull;

		public OptionalDebugParameter(T value, Function<@NotNull T, String> ifNotNull, String ifNull) {
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
