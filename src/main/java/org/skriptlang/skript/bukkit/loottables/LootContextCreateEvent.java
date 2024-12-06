package org.skriptlang.skript.bukkit.loottables;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * The event used in the ExprSecCreateLootContext section.
 */
public class LootContextCreateEvent extends Event {

	private final LootContextWrapper contextWrapper;

	public LootContextCreateEvent(LootContextWrapper context) {
		this.contextWrapper = context;
	}

	public LootContextWrapper getContextWrapper() {
		return contextWrapper;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		throw new IllegalStateException();
	}

}
