package ch.njol.skript.test.runner;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Raised by Skript when tests are run.
 */
public class SkriptTestEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
}
