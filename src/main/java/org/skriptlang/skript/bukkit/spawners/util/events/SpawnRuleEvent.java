package org.skriptlang.skript.bukkit.spawners.util.events;

import org.bukkit.block.spawner.SpawnRule;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Event to allow retrieving the spawn rule in the spawn rule sections.
 */
public class SpawnRuleEvent extends Event {

	private final SpawnRule rule;

	public SpawnRuleEvent(SpawnRule rule) {
		this.rule = rule;
	}

	/**
	 * Gets the spawn rule associated with this event.
	 * @return the spawn rule
	 */
	public SpawnRule getSpawnRule() {
		return rule;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		throw new UnsupportedOperationException();
	}

}
