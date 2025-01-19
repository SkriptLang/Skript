package org.skriptlang.skript.bukkit.spawner.util;

import ch.njol.skript.lang.util.common.AnyWeighted;
import org.bukkit.loot.LootTable;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a loot table with a weight. Weighted loot tables are used by trial spawners to pick a random loot table
 * from a list of loot tables for the reward.
 */
public class WeightedLootTable implements AnyWeighted {

	private @NotNull LootTable lootTable;
	private int weight;

	public WeightedLootTable(@NotNull LootTable lootTable, int weight) {
		this.lootTable = lootTable;
		this.weight = weight;
	}

	public void setLootTable(@NotNull LootTable lootTable) {
		this.lootTable = lootTable;
	}

	public @NotNull LootTable getLootTable() {
		return lootTable;
	}

	@Override
	public @NotNull Integer weight() {
		return weight;
	}

	@Override
	public boolean supportsWeightChange() {
		return true;
	}

	@Override
	public void setWeight(Integer weight) {
		if (weight > 0)
			this.weight = weight;
	}

}
