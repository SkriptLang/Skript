package ch.njol.skript.events.bukkit;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * @deprecated This event was never actually called.
 * It is only used as a context holder for certain events.
 */
@Deprecated(since = "INSERT VERSION", forRemoval = true)
public class ScriptEvent extends Event {
	
	public ScriptEvent() {}
	
	// Bukkit stuff
	private final static HandlerList handlers = new HandlerList();
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
	
}
