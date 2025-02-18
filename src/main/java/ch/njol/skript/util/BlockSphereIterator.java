package ch.njol.skript.util;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.util.NullableChecker;
import ch.njol.util.coll.iterator.CheckedIterator;

/**
 * @author Peter Güttinger
 */
public class BlockSphereIterator extends CheckedIterator<Block> {
	
	public BlockSphereIterator(final Location center, final double radius) {
		super(new AABB(center, radius + 0.5001, radius + 0.5001, radius + 0.5001).iterator(), new NullableChecker<Block>() {
			private final double rSquared = radius * radius * Skript.EPSILON_MULT;
			
			@Override
			public boolean check(final @Nullable Block b) {
				return b != null && center.distanceSquared(b.getLocation().add(0.5, 0.5, 0.5)) < rSquared;
			}
		});
	}
}
