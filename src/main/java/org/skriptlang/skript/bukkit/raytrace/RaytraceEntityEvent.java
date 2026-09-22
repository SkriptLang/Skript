package org.skriptlang.skript.bukkit.raytrace;

import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;
import org.jetbrains.annotations.NotNull;

/**
 * An event used to evaluate the conditions of an 'ignore entities if' section against a single entity.
 */
public class RaytraceEntityEvent extends EntityEvent {

	public RaytraceEntityEvent(@NotNull Entity entity) {
		super(entity);
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		throw new IllegalStateException();
	}

}
