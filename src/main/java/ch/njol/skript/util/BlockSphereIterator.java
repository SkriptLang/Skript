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
