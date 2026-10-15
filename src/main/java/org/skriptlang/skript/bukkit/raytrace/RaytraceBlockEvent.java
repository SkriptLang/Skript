package org.skriptlang.skript.bukkit.raytrace;

import org.bukkit.block.Block;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockEvent;
import org.jetbrains.annotations.NotNull;

/**
 * An event used to evaluate the conditions of an 'ignore blocks if' section against a single block.
 */
public class RaytraceBlockEvent extends BlockEvent {

	public RaytraceBlockEvent(@NotNull Block block) {
		super(block);
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		throw new IllegalStateException();
	}

}
