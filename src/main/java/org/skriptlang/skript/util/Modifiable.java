package org.skriptlang.skript.util;

import com.google.common.base.Preconditions;
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
	 * Returns whether this object has the specified modifier.
	 *
	 * @param modifier The modifier.
	 * @return True when {@link #modifiers()} contains the specified modifier, false if not.
	 */
	default boolean hasModifier(@NotNull Class<? extends Modifier> modifier) {
		Preconditions.checkNotNull(modifier, "modifier cannot be null");

		return modifiers().stream().anyMatch(modifier::isInstance);
	}

	/**
	 * Gets a modifier of the specified type if present.
	 *
	 * @param modifierClass The class of the modifier to retrieve
	 * @return The modifier instance.
	 * @throws NoSuchElementException If no instance of the modifier is found.
	 */
	default <M extends Modifier> @NotNull M getModifier(@NotNull Class<M> modifierClass) {
		Preconditions.checkNotNull(modifierClass, "modifierClass cannot be null");

		return modifiers().stream()
				.filter(modifierClass::isInstance)
				.map(modifierClass::cast)
				.findFirst()
				.orElseThrow(() -> new NoSuchElementException("No value present for modifier " + modifierClass.getSimpleName()));
	}

}
