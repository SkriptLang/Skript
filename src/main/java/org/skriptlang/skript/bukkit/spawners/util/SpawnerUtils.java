package org.skriptlang.skript.bukkit.spawners.util;

import ch.njol.skript.Skript;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import org.bukkit.block.Block;
import org.bukkit.block.TrialSpawner;
import org.bukkit.spawner.BaseSpawner;
import org.bukkit.spawner.Spawner;
import org.bukkit.spawner.TrialSpawnerConfiguration;
import org.jetbrains.annotations.UnknownNullability;

/**
 * Utility class for spawners.
 */
@SuppressWarnings("UnstableApiUsage")
public class SpawnerUtils {

	public static boolean IS_RUNNING_1_21_4 = Skript.isRunningMinecraft(1, 21, 4);

	public static final int DEFAULT_ACTIVATION_RANGE = 16;
	public static final int DEFAULT_MAX_NEARBY_ENTITIES = 6;
	public static final int DEFAULT_SPAWN_RANGE = 4;
	public static final int DEFAULT_SPAWN_COUNT = 4;

	public static final Timespan DEFAULT_MAX_SPAWN_DELAY = new Timespan(TimePeriod.TICK, 800);
	public static final Timespan DEFAULT_MIN_SPAWN_DELAY = new Timespan(TimePeriod.TICK, 200);
	public static final Timespan DEFAULT_COOLDOWN_LENGTH = new Timespan(TimePeriod.TICK, 36_000);
	public static final Timespan DEFAULT_TRIAL_SPAWN_DELAY = new Timespan(TimePeriod.TICK, 40);

	public static final int DEFAULT_TRIAL_ACTIVATION_RANGE = 14;
	public static final int DEFAULT_BASE_MOB_AMOUNT = 6;
	public static final int DEFAULT_BASE_PER_PLAYER_INCREMENT = 2;
	public static final int DEFAULT_CONCURRENT_MOB_AMOUNT = 2;
	public static final int DEFAULT_CONCURRENT_PER_PLAYER_INCREMENT = 1;

	/**
	 * Checks if the given object is a spawner (mob spawner or trial spawner).
	 * @param object the object to check
	 * @return whether the object is a spawner
	 */
	public static boolean isSpawner(Object object) {
		return isMobSpawner(object) || isTrialSpawner(object);
	}

	/**
	 * Gets the spawner (mob spawner or trial spawner configuration, by default non-ominous) from the given object.
	 * @param object the object to get the spawner from
	 * @return the spawner, or null if the object is not a spawner
	 * @see	#isSpawner(Object)
	 */
	public static @UnknownNullability BaseSpawner getSpawner(Object object) {
		if (isMobSpawner(object)) {
			return getMobSpawner(object);
		} else if (isTrialSpawner(object)) {
			return getTrialSpawnerConfiguration(getTrialSpawner(object));
		}

		return null;
	}

	/**
	 * Checks if the given object is a mob spawner.
	 * @param object the object to check
	 * @return whether the object is a mob spawner
	 */
	public static boolean isMobSpawner(Object object) {
		if (object instanceof Block block)
			return block.getState() instanceof Spawner;
		return object instanceof Spawner;
	}

	/**
	 * Retrieves the mob spawner from the given object.
	 * @param object the object to retrieve the mob spawner from
	 * @return the mob spawner
	 * @see #isMobSpawner(Object)
	 */
	public static Spawner getMobSpawner(Object object) {
		if (object instanceof Block block)
			return (Spawner) block.getState();
		return (Spawner) object;
	}

	/**
	 * Checks if the given object is a trial spawner.
	 *
	 * @param object the object to check
	 * @return whether the object is a trial spawner
	 * @see #getTrialSpawner(Object)
	 */
	public static boolean isTrialSpawner(Object object) {
		if (object instanceof Block block)
			return block.getState() instanceof TrialSpawner;
		return object instanceof TrialSpawner;
	}

	/**
	 * Retrieves the trial spawner from the given object.
	 *
	 * @param object the object to retrieve the trial spawner from.
	 * @return the trial spawner
	 * @see #isTrialSpawner(Object)
	 */
	public static TrialSpawner getTrialSpawner(Object object) {
		if (object instanceof Block block)
			return (TrialSpawner) block.getState();
		return (TrialSpawner) object;
	}

	/**
	 * Returns the trial spawner configuration for the given trial spawner.
	 *
	 * @param trialSpawner the trial spawner to retrieve the configuration for
	 * @param ominous whether to retrieve the ominous configuration
	 * @return the trial spawner configuration
	 */
	public static TrialSpawnerConfiguration getTrialSpawnerConfiguration(TrialSpawner trialSpawner, boolean ominous) {
		if (ominous)
			return trialSpawner.getOminousConfiguration();
		return trialSpawner.getNormalConfiguration();
	}

	/**
	 * Returns the trial spawner configuration for the given trial spawner.
	 * Automatically determines whether to retrieve the ominous configuration.
	 *
	 * @param trialSpawner the trial spawner to retrieve the configuration for
	 * @return the trial spawner configuration
	 */
	public static TrialSpawnerConfiguration getTrialSpawnerConfiguration(TrialSpawner trialSpawner) {
		return getTrialSpawnerConfiguration(trialSpawner, trialSpawner.isOminous());
	}

}
