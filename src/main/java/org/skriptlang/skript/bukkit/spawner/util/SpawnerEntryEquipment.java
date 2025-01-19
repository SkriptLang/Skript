package org.skriptlang.skript.bukkit.spawner.util;

import org.bukkit.block.spawner.SpawnerEntry.Equipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.loot.LootTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A wrapper for {@link Equipment} to allow the usage of {@link Drops}.
 */
public class SpawnerEntryEquipment {

	private @NotNull LootTable equipmentLootTable;
	private @NotNull List<Drops> drops;
	private transient @Nullable Equipment cachedEquipment;

	public SpawnerEntryEquipment(@NotNull LootTable equipmentLootTable, @NotNull List<Drops> drops) {
		this.equipmentLootTable = equipmentLootTable;
		this.drops = drops;
	}

	public Equipment getEquipment() {
		if (cachedEquipment == null) {
			// conversion to map for Equipment constructor
			Map<EquipmentSlot, Float> dropChances = new HashMap<>();
			for (Drops chance : this.drops) {
				dropChances.put(chance.getEquipmentSlot(), chance.getDropChance());
			}
			cachedEquipment = new Equipment(equipmentLootTable, dropChances);
		}
		return cachedEquipment;
	}

	public @NotNull LootTable getEquipmentLootTable() {
		return equipmentLootTable;
	}

	public @NotNull List<Drops> getDropChances() {
		return drops;
	}

	public void setEquipmentLootTable(@NotNull LootTable equipmentLootTable) {
		cachedEquipment = null;
		this.equipmentLootTable = equipmentLootTable;
	}

	public void setDropChances(@NotNull List<Drops> drops) {
		cachedEquipment = null;
		this.drops = drops;
	}

	public void addDropChance(@NotNull Drops drops) {
		cachedEquipment = null;
		this.drops.add(drops);
	}

	public void removeDropChance(@NotNull Drops drops) {
		cachedEquipment = null;
		this.drops.remove(drops);
	}

	/**
	 * A helper class to represent the drop chance for a specific equipment slot.
	 */
	public static class Drops {

		private @NotNull EquipmentSlot slot;
		private float dropChance;

		public Drops(@NotNull EquipmentSlot slot, float dropChance) {
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
