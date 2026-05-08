package org.skriptlang.skript.bukkit.entity;

import ch.njol.skript.aliases.ItemType;

/**
 * Indicates an {@link EntityData} class is comparable to an {@link ItemType}.
 */
public interface ItemTypeComparable {

	/**
	 * Whether this {@link EntityData} typed-class is of {@code itemType}.
	 * Each implementation varies on what is considered to pass.
	 * @param itemType The {@link ItemType} to check against.
	 * @return {@code true} if the {@code itemType} passes, otherwise {@code false}.
	 */
	boolean isOfItemType(ItemType itemType);

}
