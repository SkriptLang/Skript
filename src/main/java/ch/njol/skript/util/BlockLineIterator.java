package ch.njol.skript.util;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class BlockLineIterator implements Iterator<Block> {

	private final Location current;
	private final Vector end;
	private final Vector step;
	private boolean finished;

	public BlockLineIterator(Location start, Location end) {
		current = start.toCenterLocation();
		this.end = end.toCenterLocation().toVector();
		step = this.end.clone().subtract(current.toVector()).normalize();
	}

	public BlockLineIterator(Block start, Block end) {
		this(start.getLocation(), end.getLocation());
	}

	public BlockLineIterator(Location start, Vector direction, double distance) {
		this(start, start.toCenterLocation().add(direction.clone().normalize().multiply(distance)));
	}

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
		double eps = 1 + Math.ulp(1) * (Math.abs(current.getBlockX()) + Math.abs(current.getBlockZ()));
		double t = calculateExact(current.toVector()) * eps;
		current.add(step.clone().multiply(t));
		return block;
	}

	private record Plane(double a, double b, double c, double d) {
		static Plane create(Vector p1, Vector p2, Vector p3) {
			Vector v1 = p2.clone().subtract(p1);
			Vector v2 = p3.clone().subtract(p1);
			Vector normal = v1.clone().crossProduct(v2);
			double a = normal.getX();
			double b = normal.getY();
			double c = normal.getZ();
			double d = -(a * p1.getX() + b * p1.getY() + c * p1.getZ());
			return new Plane(a, b, c, d);
		}
	}

	private double calculateExact(Vector from) {
		double xSgn = Math.signum(step.getX());
		double ySgn = Math.signum(step.getY());
		double zSgn = Math.signum(step.getZ());
		Vector center = center(from);
		Vector xPlanePoint = center.clone().add(new Vector(xSgn > 0 ? 0.5 : -0.5, 0, 0));
		Vector yPlanePoint = center.clone().add(new Vector(0, ySgn > 0 ? 0.5 : -0.5, 0));
		Vector zPlanePoint = center.clone().add(new Vector(0, 0, zSgn > 0 ? 0.5 : -0.5));
		double xDist = planeDist(from, Plane.create(
			xPlanePoint,
			xPlanePoint.clone().add(new Vector(0, 1, 0)),
			xPlanePoint.clone().add(new Vector(0, 0, 1))
		));
		double yDist = planeDist(from, Plane.create(
			yPlanePoint,
			yPlanePoint.clone().add(new Vector(1, 0, 0)),
			yPlanePoint.clone().add(new Vector(0, 0, 1))
		));
		double zDist = planeDist(from, Plane.create(
			zPlanePoint,
			zPlanePoint.clone().add(new Vector(1, 0, 0)),
			zPlanePoint.clone().add(new Vector(0, 1, 0))
		));
		return Math.min(xDist, Math.min(yDist, zDist));
	}

	private double planeDist(Vector from, Plane plane) {
		double v = 0 - plane.d - plane.a() * from.getX() - plane.b() * from.getY() - plane.c() * from.getZ();
		double t = plane.a() * step.getX() + plane.b() * step.getY() + plane.c() * step.getZ();
		double r = v / t;
		if (!Double.isFinite(r)) return 2;
		return r;
	}

	private static Vector center(Vector vector) {
		Vector center = vector.clone();
		center.setX(vector.getBlockX() + 0.5);
		center.setY(vector.getBlockY() + 0.5);
		center.setZ(vector.getBlockZ() + 0.5);
		return center;
	}

}
