package org.skriptlang.skript.bukkit.spawner.events;

import org.bukkit.block.spawner.SpawnerEntry;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.skriptlang.skript.bukkit.spawner.elements.expressions.spawnerentry.ExprSecSpawnerEntry;

/**
 * The event used in {@link ExprSecSpawnerEntry}.
 */
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
