package org.skriptlang.skript.bukkit.loottables;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.Lootable;

/**
 * Utility class for loot tables.
 */
public class LootTableUtils {

	/**
	 * * Checks whether a block or entity is an instance of {@link Lootable}. This is done because a block is not an instance of Lootable, but a block state is.
	 * @param object the object to check.
	 * @return whether the object is lootable.
	 */
	public static boolean isLootable(Object object) {
		if (object instanceof Block block)
			object = block.getState();
		return object instanceof Lootable;
	}

	/**
	 * Gets the Lootable instance of an object. You should call {@link #isLootable(Object)} before calling this method.
	 * @param object the object to get the Lootable instance of.
	 * @return the Lootable instance of the object.
	 */
	public static Lootable getAsLootable(Object object) {
		if (object instanceof Block block)
			object = block.getState();
		if (object instanceof Lootable lootable)
			return lootable;
		return null;
	}

	/**
	 * Gets the loot table of an object. You should call {@link #isLootable(Object)} before calling this method.
	 * @param object the object to get the loot table of.
	 * @return returns the LootTable of the object.
	 */
	public static LootTable getLootTable(Object object) {
		return getAsLootable(object).getLootTable();
	}

	/**
	 * Updates the state of a Lootable. This is done because setting the LootTable or seed of a BlockState changes the NBT value, but is never updated.
	 * @param lootable the Lootable to update the state of.
	 */
	public static void updateState(Lootable lootable) {
		if (lootable instanceof BlockState blockState)
			blockState.update(true, false);
	}

}
