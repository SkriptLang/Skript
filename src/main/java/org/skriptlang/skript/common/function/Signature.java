package org.skriptlang.skript.common.function;

import ch.njol.skript.util.Contract;
import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
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

	default int maxParameters() {
		return parameters().size();
	}

	default int minParameters() {
		List<Parameter<?>> params = new LinkedList<>(parameters().values());

		int i = parameters().size() - 1;
		for (Parameter<?> parameter : Lists.reverse(params)) {
			if (!parameter.modifiers().contains(Parameter.Modifier.OPTIONAL)) {
				return i + 1;
			}
			i--;
		}

		return 0; // No-args function
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

