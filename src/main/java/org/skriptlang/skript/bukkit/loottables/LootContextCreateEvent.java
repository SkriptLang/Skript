package org.skriptlang.skript.bukkit.loottables;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class LootContextCreateEvent extends Event {

	private final LootContextWrapper context;

	public LootContextCreateEvent(LootContextWrapper context) {
		this.context = context;
	}

	public LootContextWrapper getWrapper() {
		return context;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		throw new IllegalStateException();
	}

}
