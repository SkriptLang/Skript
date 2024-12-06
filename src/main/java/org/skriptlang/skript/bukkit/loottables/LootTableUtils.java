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
	 * Checks whether a block or entity is an instance of Lootable. This is done because a block is not an instance of Lootable, but a block state is.
	 * @param object the object to check.
	 * @return whether the object is lootable.
	 */
	public static boolean isLootable(Object object) {
		if (object instanceof Block block)
			object = block.getState();
		return object instanceof Lootable;
	}

	/**
	 * Gets the lootable instance of an object.
	 * @param object the object to get the lootable instance of.
	 * @return the lootable instance of the object.
	 */
	public static Lootable getLootable(Object object) {
		if (object instanceof Block block)
			object = block.getState();
		if (object instanceof Lootable lootable)
			return lootable;
		return null;
	}

	/**
	 * Gets the loot table of an object. If the object is not a Lootable, null is returned.
	 * @param object the object to get the loot table of.
	 * @return returns the LootTable of the object.
	 */
	public static LootTable getLootTable(Object object) {
		if (isLootable(object))
			return getLootable(object).getLootTable();
		return null;
	}

	/**
	 * Sets the loot table of a Lootable.
	 * @param lootable the Lootable to set the loot table of.
	 * @param lootTable the loot table.
	 */
	public static void setLootTable(Lootable lootable, LootTable lootTable) {
		lootable.setLootTable(lootTable);
		updateState(lootable);
	}

	/**
	 * Clears the loot table of a Lootable.
	 * @param lootable the Lootable to clear the loot table of.
	 */
	public static void clearLootTable(Lootable lootable) {
		lootable.clearLootTable();
		updateState(lootable);
	}

	/**
	 * Sets the seed of a Lootable.
	 * @param lootable the Lootable to set the seed of.
	 * @param seed the seed.
	 */
	public static void setSeed(Lootable lootable, long seed) {
		lootable.setSeed(seed);
		updateState(lootable);
	}

	/**
	 * Updates the state of a Lootable. This is done because setting the LootTable or seed of a BlockState changes the NBT value, but is never updated.
	 * @param lootable the Lootable to update the state of.
	 */
	private static void updateState(Lootable lootable) {
		if (lootable instanceof BlockState blockState)
			blockState.update();
	}

}
