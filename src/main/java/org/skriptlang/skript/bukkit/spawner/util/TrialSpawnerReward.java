package org.skriptlang.skript.bukkit.spawner.util;

import org.bukkit.loot.LootTable;
import org.jetbrains.annotations.NotNull;

public final class TrialSpawnerReward {

	private @NotNull LootTable lootTable;
	private int weight;

	public TrialSpawnerReward(@NotNull LootTable lootTable, int weight) {
		this.lootTable = lootTable;
		this.weight = weight;
	}

	public void setWeight(int weight) {
		if (weight > 0)
			this.weight = weight;
	}

	public int getWeight() {
		return weight;
	}

	public void setLootTable(@NotNull LootTable lootTable) {
		this.lootTable = lootTable;
	}

	public @NotNull LootTable getLootTable() {
		return lootTable;
	}

}
