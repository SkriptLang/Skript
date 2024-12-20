package ch.njol.skript.events.bukkit;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * @author Peter Güttinger
 */
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
