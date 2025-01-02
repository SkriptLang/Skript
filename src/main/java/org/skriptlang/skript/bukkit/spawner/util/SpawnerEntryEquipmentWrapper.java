package org.skriptlang.skript.bukkit.spawner.util;

import org.bukkit.block.spawner.SpawnerEntry.Equipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.loot.LootTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpawnerEntryEquipmentWrapper {

	private @NotNull LootTable equipmentLootTable;
	private @NotNull List<DropChance> dropChances;
	private transient @Nullable Equipment cachedEquipment;

	public SpawnerEntryEquipmentWrapper(@NotNull LootTable equipmentLootTable, @NotNull List<DropChance> dropChances) {
		this.equipmentLootTable = equipmentLootTable;
		this.dropChances = dropChances;
	}

	public Equipment getEquipment() {
		if (cachedEquipment == null) {
			// conversion to map for Equipment constructor
			Map<EquipmentSlot, Float> dropChances = new HashMap<>();
			for (DropChance chance : this.dropChances) {
				dropChances.put(chance.getEquipmentSlot(), chance.getDropChance());
			}
			cachedEquipment = new Equipment(equipmentLootTable, dropChances);
		}
		return cachedEquipment;
	}

	public @NotNull LootTable getEquipmentLootTable() {
		return equipmentLootTable;
	}

	public @NotNull List<DropChance> getDropChances() {
		return dropChances;
	}

	public void setEquipmentLootTable(@NotNull LootTable equipmentLootTable) {
		cachedEquipment = null;
		this.equipmentLootTable = equipmentLootTable;
	}

	public void setDropChances(@NotNull List<DropChance> dropChances) {
		cachedEquipment = null;
		this.dropChances = dropChances;
	}

	public void addDropChance(@NotNull DropChance dropChance) {
		cachedEquipment = null;
		this.dropChances.add(dropChance);
	}

	public void removeDropChance(@NotNull DropChance dropChance) {
		cachedEquipment = null;
		this.dropChances.remove(dropChance);
	}

	public static class DropChance {

		private @NotNull EquipmentSlot slot;
		private float dropChance;

		public DropChance(@NotNull EquipmentSlot slot, float dropChance) {
			this.slot = slot;
			this.dropChance = dropChance;
		}

		public @NotNull EquipmentSlot getEquipmentSlot() {
			return slot;
		}

		public float getDropChance() {
			return dropChance;
		}

		public void setEquipmentSlot(@NotNull EquipmentSlot slot) {
			this.slot = slot;
		}

		public void setDropChance(float dropChance) {
			this.dropChance = dropChance;
		}

	}

}
