package org.skriptlang.skript.bukkit.spawners.util.spawnerdata;

import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import ch.njol.yggdrasil.Fields;
import ch.njol.yggdrasil.YggdrasilSerializable.YggdrasilExtendedSerializable;
import com.google.common.base.Preconditions;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.minecart.SpawnerMinecart;
import org.bukkit.spawner.Spawner;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.bukkit.spawners.util.SpawnerUtils;

import java.io.StreamCorruptedException;

/**
 * Represents the data of a {@link Spawner}, which may be a {@link CreatureSpawner} or a {@link SpawnerMinecart}
 *
 * @see SkriptTrialSpawnerData
 * @see SkriptSpawnerData
 */
public class SkriptMobSpawnerData extends SkriptSpawnerData implements YggdrasilExtendedSerializable {

	private int maxNearbyEntityCap = SpawnerUtils.DEFAULT_MAX_NEARBY_ENTITIES;
	private int spawnCount = SpawnerUtils.DEFAULT_SPAWN_COUNT;

	public SkriptMobSpawnerData() {}

	/**
	 * Creates a new SkriptSpawnerData instance from the given Bukkit {@link Spawner}.
	 * @param spawner the Bukkit spawner to convert
	 * @return a new SkriptSpawnerData instance containing the data from the Bukkit spawner
	 */
	public static SkriptMobSpawnerData fromSpawner(@NotNull Spawner spawner) {
		Preconditions.checkNotNull(spawner, "spawner cannot be null");

		SkriptMobSpawnerData data = new SkriptMobSpawnerData();

		SkriptSpawnerData.applyToSpawnerData(spawner, data);
		data.setMaxNearbyEntityCap(spawner.getMaxNearbyEntities());
		data.setSpawnCount(spawner.getSpawnCount());
		data.setMaxSpawnDelay(new Timespan(TimePeriod.TICK, spawner.getMaxSpawnDelay()));
		data.setMinSpawnDelay(new Timespan(TimePeriod.TICK, spawner.getMinSpawnDelay()));

		return data;
	}

	/**
	 * Applies this SkriptSpawnerData to the given spawner.
	 * @param spawner the spawner to apply the data to
	 */
	public void applyData(@NotNull Spawner spawner) {
		Preconditions.checkNotNull(spawner, "spawner cannot be null");

		super.applyToSpawner(spawner);
		spawner.setMaxNearbyEntities(getMaxNearbyEntityCap());
		spawner.setSpawnCount(getSpawnCount());
		spawner.setMaxSpawnDelay((int) Math.max(getMaxSpawnDelay().getAs(TimePeriod.TICK), Integer.MAX_VALUE));
		spawner.setMinSpawnDelay((int) Math.max(getMinSpawnDelay().getAs(TimePeriod.TICK), Integer.MAX_VALUE));

		if (spawner instanceof CreatureSpawner creatureSpawner)
			creatureSpawner.update(true, false);
	}

	/**
	 * Returns the maximum number of nearby similar entities that can be spawned by this spawner.
	 * <p>
	 * The default value is 6.
	 * @return the maximum nearby entity cap
	 */
	public int getMaxNearbyEntityCap() {
		return maxNearbyEntityCap;
	}

	/**
	 * Sets the maximum number of nearby similar entities that can be spawned by this spawner.
	 * <p>
	 * The default value is 6.
	 * @param maxNearbyEntityCap the maximum nearby entity cap
	 */
	public void setMaxNearbyEntityCap(int maxNearbyEntityCap) {
		this.maxNearbyEntityCap = maxNearbyEntityCap;
	}

	/**
	 * Returns the number of entities that the spawner will attempt to spawn each spawn attempt.
	 * <p>
	 * The default value is 4.
	 * @return the spawn count
	 */
	public int getSpawnCount() {
		return spawnCount;
	}

	/**
	 * Sets the number of entities that the spawner will attempt to spawn each spawn attempt.
	 * <p>
	 * The default value is 4.
	 * @param spawnCount the spawn count
	 */
	public void setSpawnCount(int spawnCount) {
		this.spawnCount = spawnCount;
	}

	/*
	 * YggdrasilExtendedSerializable
	 */

	@Override
	public Fields serialize() {
		Fields fields = super.serialize();

		fields.putPrimitive("max_nearby_entity_cap", this.maxNearbyEntityCap);
		fields.putPrimitive("spawn_count", this.spawnCount);

		return fields;
	}

	@Override
	public void deserialize(@NotNull Fields fields) throws StreamCorruptedException {
		super.deserialize(fields);

		this.maxNearbyEntityCap = fields.getPrimitive("max_nearby_entity_cap", int.class);
		this.spawnCount = fields.getPrimitive("spawn_count", int.class);
	}

}
