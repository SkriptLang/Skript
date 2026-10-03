package org.skriptlang.skript.bukkit.raytrace;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.Nullable;

/**
 * Utilities for working with the result of a raytrace.
 */
public final class RaytraceUtils {

	private RaytraceUtils() { }

	/**
	 * Converts the position a raytrace hit into a location.
	 * <br>
	 * A {@link RayTraceResult} only stores its hit position as a vector, so the world is taken from whatever was hit.
	 *
	 * @param result The result to get the hit location of.
	 * @return The location the raytrace hit, or null if it hit neither a block nor an entity.
	 */
	public static @Nullable Location toLocation(RayTraceResult result) {
		Block block = result.getHitBlock();
		Entity entity = result.getHitEntity();
		World world = block != null ? block.getWorld() : (entity != null ? entity.getWorld() : null);
		if (world == null)
			return null;
		return result.getHitPosition().toLocation(world);
	}

}
