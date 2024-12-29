package org.skriptlang.skript.bukkit.spawner.util;

import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.TrialSpawner;
import org.bukkit.entity.minecart.SpawnerMinecart;
import org.bukkit.spawner.BaseSpawner;
import org.bukkit.spawner.Spawner;
import org.jetbrains.annotations.UnknownNullability;

public class SpawnerUtils {

	public static boolean isBaseSpawner(Object object) {
		if (object instanceof Block block)
			return block.getState() instanceof BaseSpawner;
		else if (object instanceof SpawnerMinecart)
			return true;
		return object instanceof TrialSpawnerConfig;
	}

	public static boolean isSpawner(Object object) {
		if (object instanceof Block block)
			return block.getState() instanceof Spawner;
		return object instanceof SpawnerMinecart;
	}

	public static @UnknownNullability BaseSpawner getAsBaseSpawner(Object object) {
		if (object instanceof Block block)
			return (BaseSpawner) block.getState();
		else if (object instanceof SpawnerMinecart spawner)
			return spawner;
		else if (object instanceof TrialSpawnerConfig config)
			return config.getConfig();
		return null;
	}

	public static @UnknownNullability Spawner getAsSpawner(Object object) {
		if (object instanceof Block block)
			return (Spawner) block.getState();
		else if (object instanceof SpawnerMinecart spawner)
			return spawner;
		return null;
	}

	public static void updateState(Object object) {
		if (object instanceof CreatureSpawner spawner)
			spawner.update(true, false);
		else if (object instanceof TrialSpawner spawner)
			spawner.update(true, false);
	}

}
