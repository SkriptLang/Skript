package org.skriptlang.skript.bukkit.spawners.util.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.bukkit.spawners.util.spawnerdata.SkriptSpawnerData;

/**
 * Event to allow retrieving the spawner datas in the spawner data sections.
 */
public class SpawnerDataEvent<T extends SkriptSpawnerData> extends Event {

	private final T data;

	public SpawnerDataEvent(T data) {
		this.data = data;
	}

	public T getSpawnerData() {
		return data;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		throw new UnsupportedOperationException();
	}

}
