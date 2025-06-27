package ch.njol.skript.util;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.WorldUtils;
import ch.njol.util.Math2;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * AABB = Axis-Aligned Bounding Box
 * 
 * @author Peter Güttinger
 */
public class AABB implements Iterable<Block> {
	
	final World world;
	final Vector3d lowerBound, upperBound;

	//	private final static Vector EPSILON = new Vector(Skript.EPSILON, Skript.EPSILON, Skript.EPSILON);
	
	@SuppressWarnings("null")
	public AABB(final Location l1, final Location l2) {
		if (l1.getWorld() != l2.getWorld())
			throw new IllegalArgumentException("Locations must be in the same world");
		world = l1.getWorld();
		lowerBound = new Vector3d(Math.min(l1.getBlockX(), l2.getBlockX()), Math.min(l1.getBlockY(), l2.getBlockY()), Math.min(l1.getBlockZ(), l2.getBlockZ()));
		upperBound = new Vector3d(Math.max(l1.getBlockX(), l2.getBlockX()), Math.max(l1.getBlockY(), l2.getBlockY()), Math.max(l1.getBlockZ(), l2.getBlockZ()));
	}
	
	public AABB(final Block b1, final Block b2) {
		if (b1.getWorld() != b2.getWorld())
			throw new IllegalArgumentException("Blocks must be in the same world");
		world = b1.getWorld();
		lowerBound = new Vector3d(Math.min(b1.getX(), b2.getX()), Math.min(b1.getY(), b2.getY()), Math.min(b1.getZ(), b2.getZ()));
		upperBound = new Vector3d(Math.max(b1.getX(), b2.getX()), Math.max(b1.getY(), b2.getY()), Math.max(b1.getZ(), b2.getZ()));
	}
	
	@SuppressWarnings("null")
	public AABB(final Location center, final double rX, final double rY, final double rZ) {
		assert rX >= 0 && rY >= 0 && rZ >= 0 : rX + "," + rY + "," + rY;
		world = center.getWorld();
		int min = WorldUtils.getWorldMinHeight(world);
		lowerBound = new Vector3d(center.getX() - rX, Math.max(center.getY() - rY, min), center.getZ() - rZ);
		upperBound = new Vector3d(center.getX() + rX, Math.min(center.getY() + rY, world.getMaxHeight() - 1), center.getZ() + rZ);
	}

	public AABB(final World w, final Vector3d v1, final Vector3d v2) {
		world = w;
		lowerBound = v1.min(v2, new Vector3d());
		upperBound = v1.max(v2, new Vector3d());
	}
	
	public AABB(final Chunk c) {
		world = c.getWorld();
		int min = WorldUtils.getWorldMinHeight(world);
		lowerBound = c.getBlock(0, min, 0).getLocation().toVector().toVector3d();
		upperBound = c.getBlock(15, world.getMaxHeight() - 1, 15).getLocation().toVector().toVector3d();
	}
	
	public boolean contains(final Location l) {
		if (l.getWorld() != world)
			return false;
		return lowerBound.x() - Skript.EPSILON < l.getX() && l.getX() < upperBound.x() + Skript.EPSILON
				&& lowerBound.y() - Skript.EPSILON < l.getY() && l.getY() < upperBound.y() + Skript.EPSILON
				&& lowerBound.z() - Skript.EPSILON < l.getZ() && l.getZ() < upperBound.z() + Skript.EPSILON;
	}
	
	public boolean contains(final Block b) {
		return contains(b.getLocation()) && contains(b.getLocation().add(1, 1, 1));
	}
	
	public Vector3d getDimensions() {
		return upperBound.sub(lowerBound, new Vector3d());
	}
	
	public World getWorld() {
		return world;
	}
	
	/**
	 * Returns an iterator which iterates over all blocks that are in this AABB
	 */
	@Override
	public Iterator<Block> iterator() {
		return new Iterator<Block>() {
			private final int minX = (int) Math2.ceil(lowerBound.x());
			private final int minY = (int) Math2.ceil(lowerBound.y());
			private final int minZ = (int) Math2.ceil(lowerBound.z());
			private final int maxX = (int) Math2.floor(upperBound.x());
			private final int maxY = (int) Math2.floor(upperBound.y());
			private final int maxZ = (int) Math2.floor(upperBound.z());
			
			private int x = minX - 1; // next() increases x by one immediately
			private int y = minY;
			private int z = minZ;
			
			@Override
			public boolean hasNext() {
				return y <= maxY && (x != maxX || y != maxY || z != maxZ);
			}
			
			@Override
			public Block next() {
				if (!hasNext())
					throw new NoSuchElementException();
				x++;
				if (x > maxX) {
					x = minX;
					z++;
					if (z > maxZ) {
						z = minZ;
						y++;
					}
				}
				if (y > maxY)
					throw new NoSuchElementException();
				return world.getBlockAt(x, y, z);
			}
			
			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + lowerBound.hashCode();
		result = prime * result + upperBound.hashCode();
		result = prime * result + world.hashCode();
		return result;
	}
	
	@Override
	public boolean equals(final @Nullable Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof AABB))
			return false;
		final AABB other = (AABB) obj;
		if (!lowerBound.equals(other.lowerBound))
			return false;
		if (!upperBound.equals(other.upperBound))
			return false;
		if (!world.equals(other.world))
			return false;
		return true;
	}
	
}
