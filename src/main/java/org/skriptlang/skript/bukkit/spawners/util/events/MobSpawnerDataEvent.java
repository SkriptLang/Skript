package org.skriptlang.skript.bukkit.spawners.util.events;

import org.skriptlang.skript.bukkit.spawners.util.spawnerdata.SkriptMobSpawnerData;

/**
 * Event to allow retrieving the mob spawner data in the spawner data sections.
 */
public class MobSpawnerDataEvent extends SpawnerDataEvent<SkriptMobSpawnerData> {

	public MobSpawnerDataEvent(SkriptMobSpawnerData data) {
		super(data);
	}

}
