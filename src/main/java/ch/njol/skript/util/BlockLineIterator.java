package ch.njol.skript.util;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Iterates through blocks in a straight line from a start to end location (inclusive).
 * <p>
 * Given start and end locations are always cloned and block-centered.
 * Iterates through all blocks the line passes through in order from start to end location.
 */
public class BlockLineIterator implements Iterator<Block> {

	private final Location current;
	private final Vector end;
	private final Vector step;
	private boolean finished;

	/**
	 * @param start start location
	 * @param end end location
	 */
	public BlockLineIterator(Location start, Location end) {
		current = start.toCenterLocation();
		this.end = end.toCenterLocation().toVector();
		step = this.end.clone().subtract(current.toVector()).normalize();
	}

	/**
	 * @param start first block
	 * @param end last block
	 */
	public BlockLineIterator(Block start, Block end) {
		this(start.getLocation(), end.getLocation());
	}

	/**
	 * @param start start location
	 * @param direction direction to travel in
	 * @param distance maximum distance to travel
	 */
	public BlockLineIterator(Location start, Vector direction, double distance) {
		this(start, start.toCenterLocation().add(direction.clone().normalize().multiply(distance)));
	}

	/**
	 * @param start first block
	 * @param direction direction to travel in
	 * @param distance maximum distance to travel
	 */
	public BlockLineIterator(Block start, Vector direction, double distance) {
		this(start.getLocation(), direction, distance);
	}

	@Override
	public boolean hasNext() {
		return !finished;
	}

	@Override
	public Block next() {
		if (!hasNext()) throw new NoSuchElementException("Reached the final block destination");
		if (current.toCenterLocation().toVector().equals(end)) finished = true;
		Block block = current.getBlock();
		// moves the current position just slightly from the edge to the next block
		double epsilon = 1 + Math.ulp(1 + Math.abs(current.getBlockX()) + Math.abs(current.getBlockZ()));
		double t = calculateParamToNext(current.toVector()) * epsilon;
		current.add(step.clone().multiply(t));
		return block;
	}

	/**
	 * Represents a plane in three-dimensional space
	 * where (a, b, c) is the normal vector
	 * and d is the distance from the origin.
	 *
	 * @param a a (normal vector x)
	 * @param b b (normal vector y)
	 * @param c c (normal vector z)
	 * @param d distance from the origin
	 */
	private record Plane(double a, double b, double c, double d) {

		/**
		 * Creates a plane from three points.
		 * The three points must not be collinear.
		 *
		 * @param p1 first point
		 * @param p2 second point
		 * @param p3 third point
		 * @return plane passing through the three points
		 */
		static Plane create(Vector p1, Vector p2, Vector p3) {
			Vector firstPlaneVector = p2.clone().subtract(p1);
			Vector secondPlaneVector = p3.clone().subtract(p1);
			Vector normal = firstPlaneVector.clone().crossProduct(secondPlaneVector);
			double a = normal.getX();
			double b = normal.getY();
			double c = normal.getZ();
			double d = -(a * p1.getX() + b * p1.getY() + c * p1.getZ());
			return new Plane(a, b, c, d);
		}

	}

	private static final Vector POSITIVE_X_STEP = new Vector(0.5, 0, 0);
	private static final Vector NEGATIVE_X_STEP = new Vector(-0.5, 0, 0);
	private static final Vector POSITIVE_Y_STEP = new Vector(0, 0.5, 0);
	private static final Vector NEGATIVE_Y_STEP = new Vector(0, -0.5, 0);
	private static final Vector POSITIVE_Z_STEP = new Vector(0, 0, 0.5);
	private static final Vector NEGATIVE_Z_STEP = new Vector(0, 0, -0.5);

	/**
	 * Calculates the distance to the next block in the direction of {@link #step} from
	 * given point.
	 *
	 * @param from The starting point
	 * @return the minimum distance from the point {@code from} in the direction of {@link #step} to next block
	 */
	private double calculateParamToNext(Vector from) {
		Vector center = center(from);
		Vector xPlanePoint = center.clone().add(step.getX() >= 0 ? POSITIVE_X_STEP : NEGATIVE_X_STEP);
		Vector yPlanePoint = center.clone().add(step.getY() >= 0 ? POSITIVE_Y_STEP : NEGATIVE_Y_STEP);
		Vector zPlanePoint = center.clone().add(step.getZ() >= 0 ? POSITIVE_Z_STEP : NEGATIVE_Z_STEP);
		double xDist = planeDistance(from, Plane.create(
			xPlanePoint,
			xPlanePoint.clone().add(new Vector(0, 1, 0)),
			xPlanePoint.clone().add(new Vector(0, 0, 1))
		));
		double yDist = planeDistance(from, Plane.create(
			yPlanePoint,
			yPlanePoint.clone().add(new Vector(1, 0, 0)),
			yPlanePoint.clone().add(new Vector(0, 0, 1))
		));
		double zDist = planeDistance(from, Plane.create(
			zPlanePoint,
			zPlanePoint.clone().add(new Vector(1, 0, 0)),
			zPlanePoint.clone().add(new Vector(0, 1, 0))
		));

		double min = -1;
		if (Double.isFinite(xDist)) min = xDist;
		if (Double.isFinite(yDist) && (min == -1 || yDist < min)) min = yDist;
		if (Double.isFinite(zDist) && (min == -1 || zDist < min)) min = zDist;
		return min;
	}

	/**
	 * Calculates the distance between a point and plane along the step vector.
	 *
	 * @param point point
	 * @param plane plane
	 * @return distance
	 */
	private double planeDistance(Vector point, Plane plane) {
		double value = -plane.d;
		value -= plane.a() * point.getX() + plane.b() * point.getY() + plane.c() * point.getZ();
		double t = plane.a() * step.getX() + plane.b() * step.getY() + plane.c() * step.getZ();
		return value / t;
	}

	/**
	 * Creates vector at the center of a block at the coordinates provided
	 * by {@code vector}.
	 *
	 * @param vector point
	 * @return coordinates at the center of a block at given point
	 */
	private static Vector center(Vector vector) {
		Vector center = vector.clone();
		center.setX(vector.getBlockX() + 0.5);
		center.setY(vector.getBlockY() + 0.5);
		center.setZ(vector.getBlockZ() + 0.5);
		return center;
	}

}
