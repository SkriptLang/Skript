package ch.njol.skript.util;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Iterates through blocks in a straight line from a start to end location (inclusive).
 * <p>
 * Given start and end locations are always cloned but may not be block-centered.
 * Iterates through all blocks the line passes through in order from start to end location.
 */
public class BlockLineIterator implements Iterator<Block> {

	private final Vector3d current;
	private final Vector3d end;
	private final Vector3d centeredEnd;
	final Vector3d step; // package private for tests
	private final World world;
	private boolean finished;

	/**
	 * @param start start location
	 * @param end end location
	 */
	public BlockLineIterator(@NotNull Location start, @NotNull Location end) {
		this.current = start.toVector().toVector3d();
		this.world = start.getWorld();
		this.end = end.toVector().toVector3d();
		this.centeredEnd = centered(this.end);
		this.step = this.end.sub(current, new Vector3d()).normalize();
	}

	/**
	 * @param start first block
	 * @param end last block
	 */
	public BlockLineIterator(@NotNull Block start, @NotNull Block end) {
		this(start.getLocation().toCenterLocation(), end.getLocation().toCenterLocation());
	}

	/**
	 * @param start start location
	 * @param direction direction to travel in
	 * @param distance maximum distance to travel
	 */
	public BlockLineIterator(Location start, @NotNull Vector direction, double distance) {
		this(start, start.clone().add(direction.clone().normalize().multiply(distance)));
	}

	/**
	 * @param start first block
	 * @param direction direction to travel in
	 * @param distance maximum distance to travel
	 */
	public BlockLineIterator(@NotNull Block start, Vector direction, double distance) {
		this(start.getLocation().toCenterLocation(), direction, distance);
	}

	@Override
	public boolean hasNext() {
		return !finished;
	}

	@Override
	public Block next() {
		if (!hasNext()) throw new NoSuchElementException("Reached the final block destination");
		// sanity check (is the current->end vector pointing away from step)
		if (end.sub(current, new Vector3d()).dot(step) < 0) throw new NoSuchElementException("Overshot the final block!");
		// get block and check end
		Vector3d center = centered(current);
		Block block = getBlock(center, world);
		if (center.equals(centeredEnd)) finished = true;
		// calculate next position
		double t = stepsToNextFace(current, step, center) + Math.ulp(1);
		current.fma(t, step);
		return block;
	}

	/**
	 * Calculates the number of steps to the next closest block face this ray, defined by start and step, will encounter.
	 * Block faces are determined by the center vector, which is interpreted as the center of the block.
	 * @param start the current location of the ray to check.
	 * @param step the direction of the ray.
	 * @param center the center location of the block the ray is currently within.
	 * @return a scalar floating point number representing the number of times step must be added to start in order
	 * 			to arrive at the closest block face.
	 */
	static double stepsToNextFace(Vector3d start, @NotNull Vector3d step, Vector3d center) {
		Vector3d neededSteps = new Vector3d(Math.signum(step.x), Math.signum(step.y), Math.signum(step.z))
				.mulAdd(0.5, center)
				.sub(start)
				.div(step, new Vector3d()); // need to make new vector due to JOML method signature issue
		// get min component, ignoring NaN
		if (Double.isNaN(neededSteps.x))
			neededSteps.x = Double.POSITIVE_INFINITY;
		if (Double.isNaN(neededSteps.y))
			neededSteps.y = Double.POSITIVE_INFINITY;
		if (Double.isNaN(neededSteps.z))
			neededSteps.z = Double.POSITIVE_INFINITY;
		return neededSteps.get(neededSteps.minComponent());
	}

	/**
	 * Creates vector at the center of a block at the coordinates provided
	 * by {@code vector}.
	 *
	 * @param vector point
	 * @return coordinates at the center of a block at given point
	 */
	@Contract("_ -> new")
	private static Vector3d centered(@NotNull Vector3d vector) {
		return vector.floor(new Vector3d()).add(0.5, 0.5, 0.5);
	}

	/**
	 * @param vector the xyz coordinates of the block to get.
	 * @param world the world which the block should be obtained from
	 * @return the block at the given xyz coords in the given world.
	 */
	private static @NotNull Block getBlock(@NotNull Vector3d vector, @NotNull World world) {
		return Vector.fromJOML(vector).toLocation(world).getBlock();
	}

}
