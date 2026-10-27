package ch.njol.skript.events.bukkit;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when Skript starts (after everything was loaded)
 * 
 * @author Peter Güttinger
 */
public class SkriptStartEvent extends LocalVariableHoldingEvent{
	
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
