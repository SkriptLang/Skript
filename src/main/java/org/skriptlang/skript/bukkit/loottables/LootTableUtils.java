package org.skriptlang.skript.bukkit.loottables;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.Lootable;
import org.jetbrains.annotations.Nullable;

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
	 * Gets the Lootable instance of an object.
	 * @param object the object to get the Lootable instance of.
	 * @return the Lootable instance of the object.
	 */
	public static @Nullable Lootable getAsLootable(Object object) {
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
	public static @Nullable LootTable getLootTable(Object object) {
		if (isLootable(object))
			return getAsLootable(object).getLootTable();
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
	 * Clears the loot table of a Lootable. Clearing the loot table of an entity will reset it back to its default loot table.
	 * @param lootable the Lootable to clear the loot table of.
	 */
	public static void clearLootTable(Lootable lootable) {
		lootable.setLootTable(null);
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
			blockState.update(true, false);
	}

}
