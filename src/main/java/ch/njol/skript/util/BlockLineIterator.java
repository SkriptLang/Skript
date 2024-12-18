package ch.njol.skript.util;

import ch.njol.skript.bukkitutil.WorldUtils;
import ch.njol.util.Math2;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import ch.njol.util.NullableChecker;
import ch.njol.util.coll.iterator.StoppableIterator;

public class BlockLineIterator extends StoppableIterator<Block> {

	/**
	 * @param start
	 * @param end
	 * @throws IllegalStateException randomly (Bukkit bug)
	 */
	public BlockLineIterator(Block start, Block end) throws IllegalStateException {
		super(new BlockIterator(start.getWorld(), start.getLocation().toVector(),
				end.equals(start) ? new Vector(1, 0, 0) : end.getLocation().subtract(start.getLocation()).toVector(), // should prevent an error if start = end
				0, 0
			),
		new NullableChecker<Block>() {
			private final double overshotSq = Math.pow(start.getLocation().distance(end.getLocation()) + 2, 2);
			
			@Override
			public boolean check(@Nullable Block block) {
				assert block != null;
				if (block.getLocation().distanceSquared(start.getLocation()) > overshotSq)
					throw new IllegalStateException("BlockLineIterator missed the end block!");
				return block.equals(end);
			}
		}, true);
	}

	/**
	 * @param start
	 * @param direction
	 * @param distance
	 * @throws IllegalStateException randomly (Bukkit bug)
	 */
	public BlockLineIterator(Location start, Vector direction, double distance) throws IllegalStateException {
		super(new BlockIterator(start.getWorld(), start.toVector(), direction, 0, 0), new NullableChecker<Block>() {
			private final double distSq = distance * distance;
			
			@Override
			public boolean check(final @Nullable Block b) {
				return b != null && b.getLocation().add(0.5, 0.5, 0.5).distanceSquared(start) >= distSq;
			}
		}, false);
	}

	/**
	 * @param start
	 * @param direction
	 * @param distance
	 * @throws IllegalStateException randomly (Bukkit bug)
	 */
	public BlockLineIterator(Block start, Vector direction, double distance) throws IllegalStateException {
		this(start.getLocation().add(0.5, 0.5, 0.5), direction, distance);
	}

	/**
	 * Makes the vector fit within the world parameters.
	 * 
	 * @param location The original starting location.
	 * @param direction The direction of the vector that will be based on the location.
	 * @return The newly modified Vector if needed.
	 */
	private static Vector fitInWorld(Location location, Vector direction) {
		int lowest = WorldUtils.getWorldMinHeight(location.getWorld());
		int highest = location.getWorld().getMaxHeight();
		Vector vector = location.toVector();
		int y = location.getBlockY();
		if (y >= lowest && y <= highest)
			return vector;
		double newY = Math2.fit(lowest, location.getY(), highest);
		return new Vector(location.getX(), newY, location.getZ());
	}

}
