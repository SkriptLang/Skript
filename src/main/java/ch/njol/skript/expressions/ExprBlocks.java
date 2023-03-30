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
package ch.njol.skript.expressions;

import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.AABB;
import ch.njol.skript.util.BlockLineIterator;
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.iterator.ArrayIterator;
import ch.njol.util.coll.iterator.IteratorIterable;

/**
 * @author Peter Güttinger
 */
@Name("Blocks")
@Description({"Blocks relative to other blocks or between other blocks. Can be used to get blocks relative to other blocks or for looping.",
		"Blocks from/to and between will return a straight line whereas blocks within will return a cuboid."})
@Examples({"loop blocks above the player:",
		"loop blocks between the block below the player and the targeted block:",
		"set the blocks below the player, the victim and the targeted block to air",
		"set all blocks within {loc1} and {loc2} to stone",
		"set all blocks within chunk at player to air"})
@Since("1.0, 2.5.1 (within/cuboid/chunk)")
public class ExprBlocks extends SimpleExpression<Block> {
	static {
		Skript.registerExpression(ExprBlocks.class, Block.class, ExpressionType.COMBINED,
				"[(all [[of] the]|the)] blocks %direction% [%locations%]", // TODO doesn't loop all blocks?
				"[(all [[of] the]|the)] blocks from %location% [on] %direction%",
				"[(all [[of] the]|the)] blocks from %location% to %location%",
				"[(all [[of] the]|the)] blocks between %location% and %location%",
				"[(all [[of] the]|the)] blocks within %location% and %location%",
				"[(all [[of] the]|the)] blocks (in|within) %chunk%");
	}
	
	private int pattern;
	@SuppressWarnings("null")
	private Expression<?> from;
	@Nullable
	private Expression<Location> end;
	@Nullable
	private Expression<Direction> direction;
	@Nullable
	private Expression<Chunk> chunk;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
		this.pattern = matchedPattern;
		switch (matchedPattern) {
			case 0:
				direction = (Expression<Direction>) exprs[0];
				from = exprs[1];
				break;
			case 1:
				from = exprs[0];
				direction = (Expression<Direction>) exprs[1];
				break;
			case 2:
			case 3:
			case 4:
				from = exprs[0];
				end = (Expression<Location>) exprs[1];
				break;
			case 5:
				chunk = (Expression<Chunk>) exprs[0];
				break;
			default:
				assert false : matchedPattern;
				return false;
		}
		return true;
	}
	
	@SuppressWarnings("null")
	@Override
	@Nullable
	protected Block[] get(final Event e) {
		final Expression<Direction> direction = this.direction;
		if (direction != null && !from.isSingle()) {
			final Location[] ls = (Location[]) from.getArray(e);
			final Direction d = direction.getSingle(e);
			if (ls.length == 0 || d == null)
				return new Block[0];
			final Block[] bs = new Block[ls.length];
			for (int i = 0; i < ls.length; i++) {
				bs[i] = d.getRelative(ls[i]).getBlock();
			}
			return bs;
		}
		final ArrayList<Block> r = new ArrayList<>();
		final Iterator<Block> iter = iterator(e);
		if (iter == null)
			return new Block[0];
		for (final Block b : new IteratorIterable<>(iter))
			r.add(b);
		return r.toArray(new Block[r.size()]);
	}
	
	@Override
	@Nullable
	public Iterator<Block> iterator(Event event) {
		try {
			if (chunk != null) {
				Chunk chunk = this.chunk.getSingle(event);
				if (chunk != null)
					return new AABB(chunk).iterator();
			} else if (direction != null) {
				if (!from.isSingle())
					return new ArrayIterator<>(get(event));
				Object object = from.getSingle(event);
				if (object == null)
					return null;
				Location location = object instanceof Location ? (Location) object : ((Block) object).getLocation().add(0.5, 0.5, 0.5);
				Direction direction = this.direction.getSingle(event);
				if (direction == null)
					return null;
				Vector vector = object != location ? direction.getDirection((Block) object) : direction.getDirection(location);
				if (vector.isZero())
					return null;
				return new BlockLineIterator(location, vector, SkriptConfig.maxTargetBlockDistance.value());
			} else {
				final Location loc = (Location) from.getSingle(event);
				if (loc == null)
					return null;
				assert end != null;
				final Location loc2 = end.getSingle(event);
				if (loc2 == null || loc2.getWorld() != loc.getWorld())
					return null;
				if (pattern == 4)
					return new AABB(loc, loc2).iterator();
				return new BlockLineIterator(loc.getBlock(), loc2.getBlock());
			}
		} catch (final IllegalStateException ex) {
			if (ex.getMessage().equals("Start block missed in BlockIterator"))
				return null;
			throw ex;
		}
		return null;
	}
	
	@Override
	public Class<? extends Block> getReturnType() {
		return Block.class;
	}
	
	@Override
	public boolean isSingle() {
		return false;
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		final Expression<Location> end = this.end;
		if (chunk != null) {
			return "blocks within chunk " + chunk.toString(e, debug);
		} else if (pattern == 4) {
			assert end != null;
			return "blocks within " + from.toString(e, debug) + " and " + end.toString(e, debug);
		} else if (end != null) {
			return "blocks from " + from.toString(e, debug) + " to " + end.toString(e, debug);
		} else {
			final Expression<Direction> direction = this.direction;
			assert direction != null;
			return "block" + (isSingle() ? "" : "s") + " " + direction.toString(e, debug) + " " + from.toString(e, debug);
		}
	}
	
}
