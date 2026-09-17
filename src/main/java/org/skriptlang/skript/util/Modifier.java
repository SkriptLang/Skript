package org.skriptlang.skript.util;

import org.jetbrains.annotations.NotNull;

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
	 * A constraint is a modifier which validates the input of a parameter.
	 */
	@FunctionalInterface
	interface Constraint {

		/**
		 * @param input The input.
		 * @return True if this input is valid, false if not.
		 */
		boolean isValid(Object input);

	}

}
