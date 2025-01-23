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
		current.add(step);
		return block;
	}

}
