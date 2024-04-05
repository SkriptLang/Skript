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

import java.util.Iterator;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.eclipse.jdt.annotation.Nullable;

import com.google.common.collect.Lists;

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

	@Nullable
	private Expression<Direction> direction;

	@Nullable
	private Expression<Location> end;

	@Nullable
	private Expression<Chunk> chunk;
	private Expression<?> from;
	private int pattern;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
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

	@Override
	@Nullable
	protected Block[] get(Event event) {
		if (this.direction != null && !from.isSingle()) {
			Direction direction = this.direction.getSingle(event);
			if (direction == null)
				return new Block[0];
			return from.stream(event)
					.filter(Location.class::isInstance)
					.map(Location.class::cast)
					.map(direction::getRelative)
					.map(Location::getBlock)
					.toArray(Block[]::new);
		}
		Iterator<Block> iterator = iterator(event);
		if (iterator == null)
			return new Block[0];
		return Lists.newArrayList(iterator).toArray(new Block[0]);
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
				// Cannot be zero.
				if (vector.getX() == 0 && vector.getY() == 0 && vector.getZ() == 0)
					return null;
				int distance = SkriptConfig.maxTargetBlockDistance.value();
				if (this.direction instanceof ExprDirection) {
					Expression<Number> numberExpression = ((ExprDirection) this.direction).amount;
					if (numberExpression != null) {
						Number number = numberExpression.getSingle(event);
						if (number != null)
							distance = number.intValue();
					}
				}
				return new BlockLineIterator(location, vector, distance);
			} else {
				Location loc = (Location) from.getSingle(event);
				if (loc == null)
					return null;
				assert end != null;
				Location loc2 = end.getSingle(event);
				if (loc2 == null || loc2.getWorld() != loc.getWorld())
					return null;
				if (pattern == 4)
					return new AABB(loc, loc2).iterator();
				return new BlockLineIterator(loc.getBlock(), loc2.getBlock());
			}
		} catch (IllegalStateException e) {
			if (e.getMessage().equals("Start block missed in BlockIterator"))
				return null;
			throw e;
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
	public String toString(@Nullable Event event, boolean debug) {
		if (chunk != null) {
			return "blocks within chunk " + chunk.toString(event, debug);
		} else if (pattern == 4) {
			assert end != null;
			return "blocks within " + from.toString(event, debug) + " and " + end.toString(event, debug);
		} else if (end != null) {
			return "blocks from " + from.toString(event, debug) + " to " + end.toString(event, debug);
		} else {
			assert direction != null;
			return "block" + (isSingle() ? "" : "s") + " " + direction.toString(event, debug) + " " + from.toString(event, debug);
		}
	}

}
