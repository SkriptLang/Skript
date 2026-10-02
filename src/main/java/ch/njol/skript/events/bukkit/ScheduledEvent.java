package ch.njol.skript.events.bukkit;

import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Nullable;

/**
 * @author Peter Güttinger
 */
public class ScheduledEvent extends Event {
	
	@Nullable
	private final World world;
	
	public ScheduledEvent(final @Nullable World world) {
		this.world = world;
	}
	
	@Nullable
	public final World getWorld() {
		return world;
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
