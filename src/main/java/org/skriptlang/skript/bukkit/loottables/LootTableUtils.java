package org.skriptlang.skript.bukkit.loottables;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.Lootable;
import org.jetbrains.annotations.Nullable;

public class LootTableUtils {

	public static boolean isLootable(Object object) {
		if (object instanceof Block block)
			object = block.getState();
		return object instanceof Lootable;
	}

	public static Lootable getLootable(Object object) {
		if (object instanceof Block block)
			object = block.getState();
		if (object instanceof Lootable lootable)
			return lootable;
		return null;
	}

	public static void setLootTable(Lootable lootable, LootTable value) {
		lootable.setLootTable(value);
		updateState(lootable);
	}

	public static void clearLootTable(Lootable lootable) {
		lootable.clearLootTable();
		updateState(lootable);
	}

	public static void setSeed(Lootable lootable, long value) {
		lootable.setSeed(value);
		updateState(lootable);
	}

	private static void updateState(Lootable lootable) {
		if (lootable instanceof BlockState blockState)
			blockState.update();
	}

}
