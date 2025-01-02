package org.skriptlang.skript.bukkit.spawner.util;

import ch.njol.skript.lang.util.common.AnyWeight;
import org.bukkit.loot.LootTable;
import org.jetbrains.annotations.NotNull;

public class WeightedLootTable implements AnyWeight {

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
