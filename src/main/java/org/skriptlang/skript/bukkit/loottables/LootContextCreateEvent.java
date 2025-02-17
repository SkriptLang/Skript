package org.skriptlang.skript.bukkit.loottables;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.skriptlang.skript.bukkit.loottables.elements.expressions.ExprSecCreateLootContext;

/**
 * The event used in the {@link ExprSecCreateLootContext} section.
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
	public HandlerList getHandlers() {
		throw new UnsupportedOperationException();
	}

}
