package org.skriptlang.skript.common.function;

import ch.njol.skript.util.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.LinkedHashMap;
import java.util.Set;

public interface Signature<T> {

	/**
	 * @return The name of the function represented by this signature.
	 */
	@NotNull String name();

	/**
	 * @return The namespace of this signature.
	 */
	String namespace();

	/**
	 * @return The return type of this signature.
	 */
	Class<T> returnType();

	/**
	 * @return All modifiers belonging to this signature.
	 */
	@Unmodifiable
	@NotNull Set<Modifier> modifiers();

	/**
	 * @return All parameters belonging to this signature.
	 */
	@Unmodifiable
	@NotNull LinkedHashMap<String, Parameter<?>> parameters();

	/**
	 * @return The contract belonging to this signature.
	 */
	Contract contract();

	/**
	 * Returns whether this signature has the specified modifier.
	 *
	 * @param modifier The modifier.
	 * @return True when {@link #modifiers()} contains the specified modifier, false if not.
	 */
	default boolean hasModifier(Modifier modifier) {
		return modifiers().contains(modifier);
	}

	/**
	 * @return Whether this signature returns for single values.
	 */
	default boolean single() {
		return !returnType().isArray();
	}

	/**
	 * Represents a modifier that can be applied to a signature.
	 */
	interface Modifier {

		/**
		 * @return A new Modifier instance to be used as a custom flag.
		 */
		static Modifier of() {
			return new Modifier() {
			};
		}

		/**
		 * The modifier for signatures that are local.
		 */
		Modifier LOCAL = of();

	}
}

