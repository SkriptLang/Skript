package org.skriptlang.skript.common.function;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.common.function.DefaultFunction.Builder;

import java.util.Set;

/**
 * Represents a function parameter.
 *
 * @param <T> The type of the function parameter.
 */
public interface Parameter<T> {

	/**
	 * @return The name of this parameter.
	 */
	@NotNull String name();

	/**
	 * @return The type of this parameter.
	 */
	@NotNull Class<T> type();

	/**
	 * @return All modifiers belonging to this parameter.
	 */
	@Unmodifiable @NotNull Set<Modifier> modifiers();

	/**
	 * Returns whether this parameter has the specified modifier.
	 * @param modifier The modifier.
	 * @return True when {@link #modifiers()} contains the specified modifier, false if not.
	 */
	default boolean hasModifier(Modifier modifier) {
		return modifiers().contains(modifier);
	}

	/**
	 * @return Whether this parameter is for single values.
	 */
	default boolean single() {
		return !type().isArray();
	}

	/**
	 * Represents a modifier that can be applied to a parameter
	 * when constructing one using {@link Builder#parameter(String, Class, Modifier[])}}.
	 */
	interface Modifier {

		/**
		 * @return A new Modifier instance to be used as a custom flag.
		 */
		static Modifier of() {
			return new Modifier() { };
		}

		/**
		 * The modifier for parameters that are optional.
		 */
		Modifier OPTIONAL = of();

		/**
		 * The modifier for parameters that support optional keyed expressions.
		 *
		 * @see ch.njol.skript.lang.KeyProviderExpression
		 * @see ch.njol.skript.lang.KeyReceiverExpression
		 */
		Modifier KEYED = of();

	}

}
