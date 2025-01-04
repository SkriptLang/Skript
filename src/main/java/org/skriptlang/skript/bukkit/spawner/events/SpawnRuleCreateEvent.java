package org.skriptlang.skript.bukkit.spawner.events;

import org.bukkit.block.spawner.SpawnRule;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.skriptlang.skript.bukkit.spawner.elements.expressions.spawnerentry.ExprSecSpawnerEntry;

/**
 * The event used in {@link ExprSecSpawnerEntry}.
 */
public class SpawnRuleCreateEvent extends Event {

	private final SpawnRule rule;

	public SpawnRuleCreateEvent(SpawnRule rule) {
		this.rule = rule;
	}

	public SpawnRule getSpawnRule() {
		return rule;
	}

	@Override
	public HandlerList getHandlers() {
		throw new UnsupportedOperationException();
	}

}
