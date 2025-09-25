package org.skriptlang.skript.bukkit.spawners.util;

import ch.njol.skript.lang.util.common.AnyWeighted;
import com.google.common.base.Preconditions;
import org.bukkit.block.spawner.SpawnRule;
import org.bukkit.block.spawner.SpawnerEntry;
import org.bukkit.block.spawner.SpawnerEntry.Equipment;
import org.bukkit.entity.EntitySnapshot;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.loot.LootTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a Skript spawner entry, which is a wrapper around a Bukkit {@link SpawnerEntry}.
 */
public class SkriptSpawnerEntry implements AnyWeighted {

	private @NotNull EntitySnapshot entitySnapshot;
	private @Nullable SpawnRule spawnRule;
	private @Nullable LootTable equipmentLootTable;
	private @NotNull Map<EquipmentSlot, Float> dropChances = new HashMap<>();

	private int weight = 1;

	/**
	 * Creates a new SkriptSpawnerEntry with the given entity snapshot.
	 * @param entitySnapshot the entity snapshot
	 */
	public SkriptSpawnerEntry(@NotNull EntitySnapshot entitySnapshot) {
		Preconditions.checkNotNull(entitySnapshot, "snapshot cannot be null");
		this.entitySnapshot = entitySnapshot;
	}

	/**
	 * Creates a SkriptSpawnerEntry from a Bukkit SpawnerEntry.
	 * @param entry the Bukkit SpawnerEntry to convert
	 * @return a SkriptSpawnerEntry with the same properties as the Bukkit SpawnerEntry
	 */
	public static SkriptSpawnerEntry fromSpawnerEntry(@NotNull SpawnerEntry entry) {
		Preconditions.checkNotNull(entry, "entry cannot be null");

		SkriptSpawnerEntry skriptEntry = new SkriptSpawnerEntry(entry.getSnapshot());
		skriptEntry.setWeight(entry.getSpawnWeight());
		skriptEntry.setSpawnRule(entry.getSpawnRule());

		Equipment equipment = entry.getEquipment();
		if (equipment != null) {
			skriptEntry.setEquipmentLootTable(equipment.getEquipmentLootTable());
			skriptEntry.setDropChances(equipment.getDropChances());
		}

		return skriptEntry;
	}

	/**
	 * Converts a SkriptSpawnerEntry to a Bukkit SpawnerEntry.
	 * @param skriptEntry the SkriptSpawnerEntry to convert
	 * @return a Bukkit SpawnerEntry with the same properties as the SkriptSpawnerEntry
	 */
	public static SpawnerEntry toSpawnerEntry(@NotNull SkriptSpawnerEntry skriptEntry) {
		Preconditions.checkNotNull(skriptEntry, "skriptEntry cannot be null");

		SpawnerEntry entry = new SpawnerEntry(
			skriptEntry.getEntitySnapshot(),
			skriptEntry.weight().intValue(),
			skriptEntry.getSpawnRule()
		);

		LootTable lootTable = skriptEntry.getEquipmentLootTable();
		Map<EquipmentSlot, Float> dropChances = skriptEntry.getDropChances();
		if (lootTable != null)
			entry.setEquipment(new Equipment(lootTable, dropChances));

		return entry;
	}

	@Override
	public @UnknownNullability Number weight() {
		return weight;
	}

	@Override
	public boolean supportsWeightChange() {
		return true;
	}

	@Override
	public void setWeight(Number weight) {
		this.weight = weight.intValue();
	}

	/**
	 * Gets the entity snapshot of this spawner entry.
	 * @return the entity snapshot
	 */
	public @NotNull EntitySnapshot getEntitySnapshot() {
		return entitySnapshot;
	}

	/**
	 * Sets the entity snapshot of this spawner entry.
	 * @param entitySnapshot the new entity snapshot
	 */
	public void setEntitySnapshot(@NotNull EntitySnapshot entitySnapshot) {
		Preconditions.checkNotNull(entitySnapshot, "snapshot cannot be null");
		this.entitySnapshot = entitySnapshot;
	}

	/**
	 * Gets the spawn rule of this spawner entry.
	 * @return the spawn rule, or null if not set
	 */
	public @Nullable SpawnRule getSpawnRule() {
		return spawnRule;
	}

	/**
	 * Sets the spawn rule of this spawner entry.
	 * @param spawnRule the new spawn rule, or null to remove
	 */
	public void setSpawnRule(@Nullable SpawnRule spawnRule) {
		this.spawnRule = spawnRule;
	}

	/**
	 * Gets the equipment loot table of this spawner entry, which represents the loot for the entity's equipment.
	 * @return the equipment loot table, or null if not set
	 */
	public @Nullable LootTable getEquipmentLootTable() {
		return equipmentLootTable;
	}

	/**
	 * Sets the equipment loot table of this spawner entry.
	 * @param equipmentLootTable the new equipment loot table, or null to remove
	 */
	public void setEquipmentLootTable(@Nullable LootTable equipmentLootTable) {
		this.equipmentLootTable = equipmentLootTable;
	}

	/**
	 * Gets the drop chances for each equipment slot in this spawner entry.
	 * @return a map of equipment slots to their drop chances, can be empty
	 */
	public @NotNull Map<EquipmentSlot, Float> getDropChances() {
		return Map.copyOf(dropChances);
	}

	/**
	 * Sets the drop chances for each equipment slot in this spawner entry.
	 * @param dropChances a map of equipment slots to their drop chances, cannot be null
	 */
	public void setDropChances(@NotNull Map<EquipmentSlot, Float> dropChances) {
		Preconditions.checkNotNull(dropChances, "dropChances cannot be null");
		this.dropChances = new HashMap<>(dropChances);
	}

	/**
	 * Sets the drop chance for a specific equipment slot in this spawner entry.
	 * @param slot the equipment slot to set the drop chance for
	 * @param chance the drop chance to set 1 is 100%, 0 is 0%
	 */
	public void setDropChance(@NotNull EquipmentSlot slot, float chance) {
		Preconditions.checkNotNull(slot, "slot cannot be null");
		this.dropChances.put(slot, chance);
	}

	/**
	 * Removes the drop chance for a specific equipment slot in this spawner entry.
	 * @param slot the equipment slot to remove the drop chance for
	 */
	public void removeDropChance(@NotNull EquipmentSlot slot) {
		Preconditions.checkNotNull(slot, "slot cannot be null");
		this.dropChances.remove(slot);
	}

	/**
	 * Clears all drop chances in this spawner entry.
	 */
	public void clearDropChances() {
		this.dropChances.clear();
	}

}
