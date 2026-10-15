package org.skriptlang.skript.bukkit.raytrace;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * The event a raytrace section's contents are executed with.
 * <br>
 * Syntaxes that configure a raytrace restrict themselves to this event and mutate the {@link RaytraceConfig} it holds.
 */
public class RaytraceSectionEvent extends Event {

	private final RaytraceConfig config;

	public RaytraceSectionEvent(RaytraceConfig config) {
		this.config = config;
	}

	public RaytraceConfig getConfig() {
		return config;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		throw new IllegalStateException();
	}

}
