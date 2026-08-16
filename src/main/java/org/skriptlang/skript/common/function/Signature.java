package org.skriptlang.skript.common.function;

import ch.njol.skript.util.Contract;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.ApiStatus.NonExtendable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Optional;
import java.util.Set;

/**
 * Represents a function signature.
 *
 * <h2>This interface should only be extended by {@link ch.njol.skript.lang.function.Signature}.</h2>
 * <p>It will contain methods when Signature has been properly reworked.</p>
 */
@NonExtendable
@Internal
@Experimental
public interface Signature<T> {

	/**
	 * @return The name of the function.
	 */
	@NotNull String name();

	/**
	 * @return The type of this parameter.
	 */
	@Nullable Class<T> returnType();

	/**
	 * @return An unmodifiable view of all the parameters that this signature has.
	 */
	@NotNull Parameters parameters();

	/**
	 * @return The namespace of this signature.
	 */
	@Nullable String namespace();

	/**
	 * @return The contract of this signature.
	 */
	@Experimental
	Contract contract();

	/**
	 * Adds a reference to the clearing list.
	 *
	 * @param reference The reference.
	 */
	@Experimental
	void addCall(FunctionReference<?> reference);

	/**
	 * @return Whether this signature returns single values.
	 */
	default boolean isSingle() {
		Class<T> returnType = returnType();
		if (returnType == null) {
			return false;
		}
		return !returnType.isArray();
	}

	/**
	 * @return All modifiers belonging to this signature.
	 */
	@Unmodifiable
	@NotNull Set<Modifier> modifiers();

	/**
	 * Returns whether this signature has the specified modifier.
	 *
	 * @param modifier The modifier.
	 * @return True when {@link #modifiers()} contains the specified modifier, false if not.
	 */
	default boolean hasModifier(Class<? extends Modifier> modifier) {
		return modifiers().stream().anyMatch(modifier::isInstance);
	}

	/**
	 * Returns whether this signature has the specified modifier.
	 */
	default <MT extends Modifier> Optional<MT> getModifier(Class<MT> modifier) {
		return modifiers().stream()
				.filter(modifier::isInstance)
				.map(modifier::cast)
				.findAny();
	}

	/**
	 * Represents a modifier that can be applied to a function signature.
	 */
	interface Modifier {

		class Local implements Modifier { }

	}

}
