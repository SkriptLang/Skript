package org.skriptlang.skript.bukkit.spawners.util.events;

import org.skriptlang.skript.bukkit.spawners.util.spawnerdata.SkriptTrialSpawnerData;

/**
 * Event to allow retrieving the trial spawner data in the spawner data sections.
 */
public class TrialSpawnerDataEvent extends SpawnerDataEvent<SkriptTrialSpawnerData> {

	public TrialSpawnerDataEvent(SkriptTrialSpawnerData data) {
		super(data);
	}

}
