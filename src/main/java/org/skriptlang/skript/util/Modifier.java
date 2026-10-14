package org.skriptlang.skript.util;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Something that modifies a class implementing {@link Modifiable}.
 */
public interface Modifier {

	/**
	 * The priority used when using this modifier in a string representation.
	 *
	 * @return The priority.
	 */
	@NotNull Priority priority();

	/**
	 * @return The modifier as a human-readable, formatted string.
	 */
	@NotNull String toFormattedString();

	/**
	 * A modifier constraint allows validating of the input.
	 */
	@FunctionalInterface
	interface Constraint {

		/**
		 * Checks whether the input is valid and returns an error message if not.
		 * @param input The input.
		 * @return An error message for invalid input or an empty optional for a valid input.
		 */
		@NotNull Optional<String> validate(Object input);

		/**
		 * Checks whether the input is valid.
		 * @param input The input.
		 * @return True when the input is valid, false if not.
		 */
		default boolean isValid(Object input) {
			return validate(input).isEmpty();
		}

	}

}
