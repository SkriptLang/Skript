package org.skriptlang.skript.bukkit.spawner.events;

import org.bukkit.block.spawner.SpawnerEntry;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SpawnerEntryCreateEvent extends Event {

	private final SpawnerEntry entry;

	public SpawnerEntryCreateEvent(SpawnerEntry entry) {
		this.entry = entry;
	}

	public SpawnerEntry getSpawnerEntry() {
		return entry;
	}

	@Override
	public HandlerList getHandlers() {
		throw new UnsupportedOperationException();
	}

}
