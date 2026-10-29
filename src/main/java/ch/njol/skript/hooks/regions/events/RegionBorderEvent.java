package ch.njol.skript.hooks.regions.events;

import ch.njol.skript.hooks.regions.classes.Region;
import ch.njol.skript.variables.LocalVariableHoldingEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * @author Peter Güttinger
 */
public class RegionBorderEvent extends LocalVariableHoldingEvent implements Cancellable {
	
	private final Region region;
	final Player player;
	private final boolean enter;
	
	public RegionBorderEvent(final Region region, final Player player, final boolean enter) {
		this.region = region;
		this.player = player;
		this.enter = enter;
	}
	
	public boolean isEntering() {
		return enter;
	}
	
	public Region getRegion() {
		return region;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	private boolean cancelled = false;
	
	@Override
	public boolean isCancelled() {
		return cancelled;
	}
	
	@Override
	public void setCancelled(final boolean cancel) {
		cancelled = cancel;
	}
	
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
