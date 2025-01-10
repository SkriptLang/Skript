package org.skriptlang.skript.bukkit.spawner.util;

import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.TrialSpawner;
import org.bukkit.entity.minecart.SpawnerMinecart;
import org.bukkit.spawner.BaseSpawner;
import org.bukkit.spawner.Spawner;
import org.jetbrains.annotations.UnknownNullability;

/**
 * Utility class for spawners.
 */
public class SpawnerUtils {

	/**
	 * Returns whether the object is an instance of {@link BaseSpawner}. Base spawners are creature spawners,
	 * spawner minecarts and trial spawner configurations. Note that this returns true for {@link TrialSpawnerConfig}.
	 * @param object the object
	 * @return whether the object is a base spawner
	 */
	public static boolean isBaseSpawner(Object object) {
		if (object instanceof BaseSpawner) {
			return true;
		} else if (object instanceof Block block) {
			return block.getState() instanceof BaseSpawner;
		}
		return object instanceof TrialSpawnerConfig;
	}

	/**
	 * Returns whether the object is an instance of Spawner. Spawners are creature spawners and spawner minecarts.
	 * @param object the object
	 * @return whether the object is a Spawner
	 */
	public static boolean isSpawner(Object object) {
		if (object instanceof Block block)
			return block.getState() instanceof Spawner;
		return object instanceof SpawnerMinecart;
	}

	/**
	 * Returns whether the object is an instance of {@link TrialSpawner}. This also returns true for {@link TrialSpawnerConfig}.
	 * @param object
	 * @return whether the object is a TrialSpawner
	 */
	public static boolean isTrialSpawner(Object object) {
		if (object instanceof TrialSpawnerConfig) {
			return true;
		} else if (object instanceof Block block) {
			return block.getState() instanceof TrialSpawner;
		}
		return object instanceof TrialSpawner;
	}

	/**
	 * Returns the object as a {@link BaseSpawner}.
	 * @param object the object
	 * @return the object as a base spawner
	 * @see #isBaseSpawner(Object)
	 */
	public static @UnknownNullability BaseSpawner getAsBaseSpawner(Object object) {
		if (object instanceof Block block) {
			return (BaseSpawner) block.getState();
		} else if (object instanceof SpawnerMinecart spawner) {
			return spawner;
		} else if (object instanceof TrialSpawnerConfig config) {
			return config.config();
		}
		return null;
	}

	/**
	 * Returns the object as a {@link Spawner}.
	 * @param object the object
	 * @return the object as a spawner
	 * @see #isSpawner(Object)
	 */
	public static @UnknownNullability Spawner getAsSpawner(Object object) {
		if (object instanceof Block block) {
			return (Spawner) block.getState();
		} else if (object instanceof SpawnerMinecart spawner) {
			return spawner;
		}
		return null;
	}

	/**
	 * Returns the object as a {@link TrialSpawner}. This also gets the state of a {@link TrialSpawnerConfig}.
	 * @param object the object
	 * @return the object as a trial spawner
	 * @see #isTrialSpawner(Object)
	 */
	public static @UnknownNullability TrialSpawner getAsTrialSpawner(Object object) {
		if (object instanceof Block block)
			object = block.getState();

		if (object instanceof TrialSpawner spawner)
			return spawner;

		if (object instanceof TrialSpawnerConfig config)
			return config.state();

		return null;
	}

	/**
	 * Returns the current trial spawner configuration of the spawner. I.e. ominous or regular.
	 * @param spawner the spawner
	 * @return the current trial spawner configuration
	 */
	public static TrialSpawnerConfig getCurrentTrialConfig(TrialSpawner spawner) {
		if (spawner.isOminous())
			return new TrialSpawnerConfig(spawner.getOminousConfiguration(), spawner, true);
		return new TrialSpawnerConfig(spawner.getNormalConfiguration(), spawner, false);
	}

	/**
	 * Updates the state of the spawner. This is used for {@link CreatureSpawner} and {@link TrialSpawner}.
	 * @param state the state
	 */
	public static void updateState(Object state) {
		if (state instanceof CreatureSpawner spawner) {
			spawner.update(true, false);
		} else if (state instanceof TrialSpawnerConfig config) {
			config.state().update(true, false);
		} else if (state instanceof TrialSpawner spawner) {
			spawner.update(true, false);
		}
	}

}
