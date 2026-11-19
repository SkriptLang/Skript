package ch.njol.skript.events.bukkit;

import ch.njol.skript.variables.LocalVariableHoldingEvent;
import org.bukkit.event.HandlerList;

/**
 * Called when Skript stops (before anything is unloaded)
 * 
 * @author Peter Güttinger
 */
public class SkriptStopEvent extends LocalVariableHoldingEvent {
	
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
