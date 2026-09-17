package org.skriptlang.skript.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.NoSuchElementException;

/**
 * An object that can be modified with modifiers extending {@link Modifier}.
 */
public interface Modifiable {

	/**
	 * @return All modifiers belonging to this parameter.
	 */
	@UnmodifiableView
	@NotNull Collection<? extends Modifier> modifiers();

		/**
	 * Returns whether this parameter has the specified modifier.
	 *
	 * @param modifier The modifier.
	 * @return True when {@link #modifiers()} contains the specified modifier, false if not.
	 */
	default boolean hasModifier(@NotNull Class<? extends Modifier> modifier) {
		return modifiers().stream().anyMatch(modifier::isInstance);
	}

	/**
	 * Gets a modifier of the specified type if present.
	 *
	 * @param modifierClass The class of the modifier to retrieve
	 * @return The modifier instance.
	 * @throws NoSuchElementException If no value is found for the modifier.
	 */
	default <M extends Modifier> @NotNull M getModifier(@NotNull Class<M> modifierClass) {
		return modifiers().stream()
				.filter(modifierClass::isInstance)
				.map(modifierClass::cast)
				.findFirst()
				.orElseThrow(() -> new NoSuchElementException("No value present for modifier " + modifierClass.getSimpleName()));
	}

}
