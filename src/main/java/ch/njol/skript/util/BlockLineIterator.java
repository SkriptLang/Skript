/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.util;

import ch.njol.skript.Skript;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.bukkitutil.WorldUtils;
import ch.njol.util.Math2;
import ch.njol.util.NullableChecker;
import ch.njol.util.coll.iterator.StoppableIterator;

public class BlockLineIterator extends StoppableIterator<Block> {

	/**
	 * @param start
	 * @param end
	 * @throws IllegalStateException randomly (Bukkit bug)
	 */
	public BlockLineIterator(Block start, Block end) throws IllegalStateException {
		super(new BlockIterator(
					start.getWorld(),
					start.getLocation().toVector(),
					end.equals(start) ? new Vector(1, 0, 0) : end.getLocation().subtract(start.getLocation()).toVector(),
					0, 0
			), // should prevent an error if start = end
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
        return location.toVector();
	}

}
