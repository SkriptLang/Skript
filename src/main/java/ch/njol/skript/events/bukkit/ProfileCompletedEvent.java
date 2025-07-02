package ch.njol.skript.events.bukkit;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ProfileCompletedEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	private final String sectionName;
	private final double durationMs;

	public ProfileCompletedEvent(String sectionName, double durationMs) {
		this.sectionName = sectionName;
		this.durationMs = durationMs;
	}

	public String getName() {
		return sectionName;
	}

	public double getDurationMs() {
		return durationMs;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
