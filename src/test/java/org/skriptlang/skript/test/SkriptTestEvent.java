package org.skriptlang.skript.test;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Raised by Skript when tests are run.
 * Only intended to be used for context, not actual event calls.
 */
public class SkriptTestEvent extends Event {
	
	@Override
	public @NotNull HandlerList getHandlers() {
		throw new UnsupportedOperationException();
	}
	
}
