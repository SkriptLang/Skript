package org.skriptlang.skript.bukkit.spawners.util.spawnerdata;

import ch.njol.skript.util.Timespan;
import ch.njol.yggdrasil.Fields;
import com.google.common.base.Preconditions;
import org.bukkit.spawner.BaseSpawner;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.bukkit.spawners.util.SkriptSpawnerEntry;
import org.skriptlang.skript.bukkit.spawners.util.SpawnerUtils;

import java.io.StreamCorruptedException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Abstract class representing the data of a trial spawner or a regular spawner.
 */
public abstract class SkriptSpawnerData {

	private int activationRange = SpawnerUtils.DEFAULT_ACTIVATION_RANGE;
	private int spawnRange = SpawnerUtils.DEFAULT_SPAWN_RANGE;

	private Timespan minSpawnDelay = SpawnerUtils.DEFAULT_MIN_SPAWN_DELAY;
	private Timespan maxSpawnDelay = SpawnerUtils.DEFAULT_MAX_SPAWN_DELAY;

	private @NotNull Set<SkriptSpawnerEntry> spawnerEntries = new HashSet<>();

	/**
	 * Applies the spawner data from the given spawner to this SkriptSpawnerData instance.
	 * @param spawner the spawner to apply the data from
	 */
	protected static void applyToSpawnerData(@NotNull BaseSpawner spawner, @NotNull SkriptSpawnerData data) {
		Preconditions.checkNotNull(spawner, "spawner cannot be null");

		data.setActivationRange(spawner.getRequiredPlayerRange());
		data.setSpawnRange(spawner.getSpawnRange());
		data.setSpawnerEntries(spawner.getPotentialSpawns().stream()
			.map(SkriptSpawnerEntry::fromSpawnerEntry)
			.collect(Collectors.toSet())
		);
	}

	/**
	 * Applies the current spawner data to the given spawner.
	 * @param spawner the spawner to apply the data to
	 */
	protected void applyToSpawner(@NotNull BaseSpawner spawner) {
		Preconditions.checkNotNull(spawner, "spawner cannot be null");

		spawner.setRequiredPlayerRange(getActivationRange());
		spawner.setSpawnRange(getSpawnRange());
		if (!getSpawnerEntries().isEmpty()) {
			spawner.setPotentialSpawns(getSpawnerEntries().stream()
				.map(SkriptSpawnerEntry::toSpawnerEntry)
				.toList()
			);
		}
	}

	/**
	 * The distance a player must be from the spawner for it to be activated.
	 * <p>
	 * If the value is less than or equal to 0, the spawner will always be active (given that there are players online).
	 * <p>
	 * The default value is 16.
	 * @return the activation range
	 */
	public int getActivationRange() {
		return activationRange;
	}

	/**
	 * Sets the distance a player must be from the spawner for it to be activated.
	 * <p>
	 * If the value is less than or equal to 0, the spawner will always be active (given that there are players online).
	 * <p>
	 * The default value is 16.
	 * @param activationRange the activation range
	 */
	public void setActivationRange(int activationRange) {
		this.activationRange = activationRange;
	}

	/**
	 * Gets the radius around which the spawner will attempt to spawn mobs.
	 * <p>
	 * This area is square, includes the block the spawner is in, and is centered on the spawner's x, z coordinates - not the spawner itself.
	 * <p>
	 * The default value is 4.
	 * @return the spawn range
	 */
	public int getSpawnRange() {
		return spawnRange;
	}

	/**
	 * Sets the radius around which the spawner will attempt to spawn mobs.
	 * <p>
	 * This area is square, includes the block the spawner is in, and is centered on the spawner's x, z coordinates - not the spawner itself.
	 * <p>
	 * The default value is 4.
	 * @param spawnRange the spawn range
	 */
	public void setSpawnRange(int spawnRange) {
		Preconditions.checkArgument(spawnRange > 0, "Spawn range must be > 0");
		this.spawnRange = spawnRange;
	}

	/**
	 * Returns the maximum spawn delay of the spawner.
	 * <br>
	 * The spawn delay for regular spawners is chosen randomly between the minimum and maximum spawn delays,
	 * which determines how long the spawner will wait before attempting to spawn entities again.
	 * <p>
	 * The maximum spawn delay is always greater than or equal to the minimum spawn delay.
	 * <p>
	 * The default value for regular spawners is 40 seconds (800 ticks).
	 * @return the maximum spawn delay
	 */
	public @NotNull Timespan getMaxSpawnDelay() {
		return maxSpawnDelay;
	}

	/**
	 * Sets the maximum spawn delay of the spawner.
	 * <br>
	 * The spawn delay for regular spawners is chosen randomly between the minimum and maximum spawn delays,
	 * which determines how long the spawner will wait before attempting to spawn entities again.
	 * <p>
	 * The maximum spawn delay must be greater than or equal to the minimum spawn delay.
	 * <p>
	 * The default value for regular spawners is 40 seconds (800 ticks).
	 * <p>
	 * For trial spawners, the minimum and maximum spawn delays are always identical. This results in a fixed delay,
	 * rather than a random range.
	 * <p>
	 * The default value for trial spawners is 2 seconds (40 ticks).
	 * @param maxSpawnDelay the maximum spawn delay to set
	 */
	public void setMaxSpawnDelay(@NotNull Timespan maxSpawnDelay) {
		Preconditions.checkNotNull(maxSpawnDelay, "Maximum spawn delay cannot be null");
		Preconditions.checkArgument(maxSpawnDelay.compareTo(minSpawnDelay) >= 0,
			"Maximum spawn delay cannot be less than minimum spawn delay");
		this.maxSpawnDelay = maxSpawnDelay;
	}

	/**
	 * Returns the minimum spawn delay of the spawner.
	 * <br>
	 * The spawn delay for regular spawners is chosen randomly between the minimum and maximum spawn delays,
	 * which determines how long the spawner will wait before attempting to spawn entities again.
	 * <p>
	 * The minimum spawn delay is always less than or equal to the maximum spawn delay.
	 * <p>
	 * The default value for regular spawners is 10 seconds (200 ticks).
	 * <p>
	 * For trial spawners, the minimum and maximum spawn delays are always identical. This results in a fixed delay,
	 * rather than a random range.
	 * <p>
	 * The default value for trial spawners is 2 seconds (40 ticks).
	 * @return the minimum spawn delay
	 */
	public @NotNull Timespan getMinSpawnDelay() {
		return minSpawnDelay;
	}

	/**
	 * Sets the minimum spawn delay of the spawner.
	 * <br>
	 * The spawn delay for regular spawners is chosen randomly between the minimum and maximum spawn delays,
	 * which determines how long the spawner will wait before attempting to spawn entities again.
	 * <p>
	 * The minimum spawn delay must not be greater than the maximum spawn delay.
	 * <p>
	 * The default value for regular spawners is 10 seconds (200 ticks).
	 * <p>
	 * For trial spawners, the minimum and maximum spawn delays are always identical. This results in a fixed delay,
	 * rather than a random range.
	 * <p>
	 * The default value for trial spawners is 2 seconds (40 ticks).
	 * @param minSpawnDelay the minimum spawn delay to set
	 */
	public void setMinSpawnDelay(@NotNull Timespan minSpawnDelay) {
		Preconditions.checkNotNull(minSpawnDelay, "Minimum spawn delay cannot be null");
		Preconditions.checkArgument(minSpawnDelay.compareTo(maxSpawnDelay) <= 0,
			"Minimum spawn delay cannot be greater than the maximum spawn delay");
		this.minSpawnDelay = minSpawnDelay;
	}

	/**
	 * Gets the set of spawner entries that the spawner will use to spawn entities.
	 * <p>
	 * If this is not empty, the spawner will use these entries to determine what entities to spawn.
	 * @return a set of spawner entries, or an empty set
	 */
	public @NotNull Set<SkriptSpawnerEntry> getSpawnerEntries() {
		return Set.copyOf(spawnerEntries);
	}

	/**
	 * Sets the list of spawner entries that the spawner will use to spawn entities.
	 * <p>
	 * If this is not empty, the spawner will use these entries to determine what entities to spawn.
	 * @param spawnerEntries the list of spawner entries to set, or an empty list to clear
	 */
	public void setSpawnerEntries(@NotNull Set<SkriptSpawnerEntry> spawnerEntries) {
		Preconditions.checkNotNull(spawnerEntries, "spawnerEntries cannot be null");
		this.spawnerEntries = new HashSet<>(spawnerEntries);
	}

	/**
	 * Adds spawner entries to the list of spawner entries.
	 * @param spawnerEntries the spawner entry to add
	 */
	public void addSpawnerEntries(@NotNull Set<SkriptSpawnerEntry> spawnerEntries) {
		Preconditions.checkNotNull(spawnerEntries, "spawnerEntry cannot be null");
		this.spawnerEntries.addAll(spawnerEntries);
	}

	/**
	 * Adds a single spawner entry to the list of spawner entries.
	 * @param spawnerEntry the spawner entry to add
	 */
	public void addSpawnerEntry(@NotNull SkriptSpawnerEntry spawnerEntry) {
		Preconditions.checkNotNull(spawnerEntry, "spawnerEntry cannot be null");
		this.spawnerEntries.add(spawnerEntry);
	}

	/**
	 * Removes spawner entries from the list of spawner entries.
	 * @param spawnerEntries the spawner entry to remove
	 */
	public void removeSpawnerEntries(@NotNull Set<SkriptSpawnerEntry> spawnerEntries) {
		Preconditions.checkNotNull(spawnerEntries, "spawnerEntry cannot be null");
		this.spawnerEntries.removeAll(spawnerEntries);
	}

	/**
	 * Removes a single spawner entry from the list of spawner entries.
	 * @param spawnerEntry the spawner entry to remove
	 */
	public void removeSpawnerEntry(@NotNull SkriptSpawnerEntry spawnerEntry) {
		Preconditions.checkNotNull(spawnerEntry, "spawnerEntry cannot be null");
		this.spawnerEntries.remove(spawnerEntry);
	}

	/**
	 * Clears the list of spawner entries.
	 * <p>
	 * This will remove all entries and set the spawner to not use any entries for spawning.
	 */
	public void clearSpawnerEntries() {
		spawnerEntries.clear();
	}

	/*
	 * Serialization
	 */

	protected Fields serialize() {
		Fields fields = new Fields();
		fields.putPrimitive("activation_range", this.activationRange);
		fields.putPrimitive("spawn_range", this.spawnRange);
		fields.putObject("min_spawn_delay", this.minSpawnDelay);
		fields.putObject("max_spawn_delay", this.maxSpawnDelay);

		int count = 0;
		for (SkriptSpawnerEntry entry : this.spawnerEntries) {
			fields.putObject("spawner_entry_" + count, entry);
			count++;
		}

		return fields;
	}

	protected void deserialize(@NotNull Fields fields) throws StreamCorruptedException {
		this.activationRange = fields.getPrimitive("activation_range", int.class);
		this.spawnRange = fields.getPrimitive("spawn_range", int.class);

		this.minSpawnDelay = fields.getObject("min_spawn_delay", Timespan.class);
		this.maxSpawnDelay = fields.getObject("max_spawn_delay", Timespan.class);

		int count = 0;
		while (fields.contains("spawner_entry_" + count)) {
			this.spawnerEntries.add(fields.getObject("spawner_entry_" + count, SkriptSpawnerEntry.class));
			count++;
		}
	}

}
