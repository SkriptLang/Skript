package ch.njol.skript.events.bukkit;

import ch.njol.skript.variables.LocalVariableHoldingEvent;
import org.bukkit.event.HandlerList;

/**
 * @author Peter Güttinger
 */
public class ScriptEvent extends LocalVariableHoldingEvent {
	
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
