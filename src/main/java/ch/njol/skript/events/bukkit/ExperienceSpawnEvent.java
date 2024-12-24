package ch.njol.skript.events.bukkit;

import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ExperienceSpawnEvent extends Event implements Cancellable {
	
	private int exp;
	private final Location location;
	private boolean cancelled = false;
	
	public ExperienceSpawnEvent(int exp, Location location) {
		this.exp = exp;
		this.location = location;
	}
	
	public int getSpawnedXP() {
		return exp;
	}
	
	public void setSpawnedXP(int xp) {
		this.exp = Math.max(0, xp);
	}
	
	public Location getLocation() {
		return location;
	}
	
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
