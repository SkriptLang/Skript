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

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;

@Name("Direction")
@Description("A helper expression for the <a href='classes.html#direction'>direction type</a>.")
@Examples({"thrust the player upwards",
		"set the block behind the player to water",
		"loop blocks above the player:",
		"	set {_rand} to a random integer between 1 and 10",
		"	set the block {_rand} meters south east of the loop-block to stone",
		"block in horizontal facing of the clicked entity from the player is air",
		"spawn a creeper 1.5 meters horizontally behind the player",
		"spawn a TNT 5 meters above and 2 meters horizontally behind the player",
		"thrust the last spawned TNT in the horizontal direction of the player with speed 0.2",
		"push the player upwards and horizontally forward at speed 0.5",
		"push the clicked entity in in the direction of the player at speed -0.5",
		"open the inventory of the block 2 blocks below the player to the player",
		"teleport the clicked entity behind the player",
		"grow a regular tree 2 meters horizontally behind the player"})
@Since("1.0 (basic), 2.0 (extended), INSERT VERSION (direction from)")
public class ExprDirection extends SimpleExpression<Direction> {

	private final static BlockFace[] byMark = new BlockFace[] {
			BlockFace.UP, BlockFace.DOWN,
			BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST,
			BlockFace.NORTH_EAST, BlockFace.NORTH_WEST, BlockFace.SOUTH_EAST, BlockFace.SOUTH_WEST};
	private final static int UP = 0, DOWN = 1,
			NORTH = 2, SOUTH = 3, EAST = 4, WEST = 5,
			NORTH_EAST = 6, NORTH_WEST = 7, SOUTH_EAST = 8, SOUTH_WEST = 9;

	static {
		// TODO think about parsing statically & dynamically (also in general)
		// "at": see LitAt
		Skript.registerExpression(ExprDirection.class, Direction.class, ExpressionType.COMBINED,
				"[%-number% [(block|met(er|re))[s]] [to the]] (" +
						NORTH + "¦north[(-| |)(" + (NORTH_EAST ^ NORTH) + "¦east|" + (NORTH_WEST ^ NORTH) + "¦west)][(ward(s|ly|)|er(n|ly|))] [of]" +
						"|" + SOUTH + "¦south[(-| |)(" + (SOUTH_EAST ^ SOUTH) + "¦east|" + (SOUTH_WEST ^ SOUTH) + "¦west)][(ward(s|ly|)|er(n|ly|))] [of]" +
						"|(" + EAST + "¦east|" + WEST + "¦west)[(ward(s|ly|)|er(n|ly|))] [of]" +
						"|" + UP + "¦above|" + UP + "¦over|(" + UP + "¦up|" + DOWN + "¦down)[ward(s|ly|)]|" + DOWN + "¦below|" + DOWN + "¦under[neath]|" + DOWN + "¦beneath" +
						") [%-direction%]",
				"[%-number% [(block|met(er|re))[s]]] in [the] (0¦direction|1¦horizontal direction|2¦facing|3¦horizontal facing) of %entity/block% [of|from]",
				"[%-number% [(block|met(er|re))[s]]] in %entity/block%'[s] (0¦direction|1¦horizontal direction|2¦facing|3¦horizontal facing) [of|from]",
				"[%-number% [(block|met(er|re))[s]]] (0¦in[ ]front [of]|0¦forward[s]|2¦behind|2¦backwards|[to the] (1¦right|-1¦left) [of])",
				"[%-number% [(block|met(er|re))[s]]] horizontal[ly] (0¦in[ ]front [of]|0¦forward[s]|2¦behind|2¦backwards|to the (1¦right|-1¦left) [of])",
				"[[[in] the] direction] (of|from) %location% to %location%");
	}

	@Nullable
	Expression<Number> amount;

	@Nullable
	private Expression<Location> from;

	@Nullable
	private Expression<?> relativeTo;

	@Nullable
	private ExprDirection next; // If this direction concatenates with more directions.

	@Nullable
	private Vector direction;

	private boolean horizontal;
	private boolean facing;
	private double yaw;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		switch (matchedPattern) {
			case 0:
				direction = new Vector(byMark[parseResult.mark].getModX(), byMark[parseResult.mark].getModY(), byMark[parseResult.mark].getModZ());
				if (exprs[1] != null) {
					if (!(exprs[1] instanceof ExprDirection) || ((ExprDirection) exprs[1]).direction == null)
						return false;
					next = (ExprDirection) exprs[1];
				}
				break;
			case 1:
			case 2:
				relativeTo = exprs[1];
				horizontal = parseResult.mark % 2 != 0;
				facing = parseResult.mark >= 2;
				break;
			case 3:
			case 4:
				yaw = Math.PI / 2 * parseResult.mark;
				horizontal = matchedPattern == 4;
				break;
			case 5:
				from = (Expression<Location>) exprs[0];
				relativeTo = exprs[1];
				return true; // No amount
		}
		amount = (Expression<Number>) exprs[0];
		return true;
	}

	@Override
	@Nullable
	protected Direction[] get(Event event) {
		Number number = amount != null ? amount.getSingle(event) : 1;
		if (number == null)
			return new Direction[0];
		double length = number.doubleValue();
		if (direction != null) {
			Vector vector = this.direction.clone().multiply(length);
			ExprDirection addDirection = next;
			while (addDirection != null) {
				Number number2 = addDirection.amount != null ? addDirection.amount.getSingle(event) : 1;
				if (number2 == null)
					return new Direction[0];
				assert addDirection.direction != null; // checked in init()
				vector.add(addDirection.direction.clone().multiply(number2.doubleValue()));
				addDirection = addDirection.next;
			}
			assert vector != null;
			return CollectionUtils.array(new Direction(vector));
		} else if (relativeTo != null) {
			Object object = relativeTo.getSingle(event);
			if (object == null)
				return new Direction[0];
			if (object instanceof Block) {
				BlockFace facing = Direction.getFacing((Block) object);
				if (facing == BlockFace.SELF || horizontal && (facing == BlockFace.UP || facing == BlockFace.DOWN))
					return CollectionUtils.array(Direction.ZERO);
				return CollectionUtils.array(new Direction(facing, length));
			} else if (object instanceof Entity) {
				Location location = ((Entity) object).getLocation();
				if (!horizontal) {
					if (!facing) {
						Vector vector = location.getDirection().normalize().multiply(length);
						assert vector != null;
						return CollectionUtils.array(new Direction(vector));
					}
					double pitch = Direction.pitchToRadians(location.getPitch());
					assert pitch >= -Math.PI / 2 && pitch <= Math.PI / 2;
					if (pitch > Math.PI / 4)
						return CollectionUtils.array(new Direction(new double[] {0, length, 0}));
					if (pitch < -Math.PI / 4)
						return CollectionUtils.array(new Direction(new double[] {0, -length, 0}));
				}
				double yaw = Direction.yawToRadians(location.getYaw());
				if (horizontal && !facing) {
					return CollectionUtils.array(new Direction(new double[] {Math.cos(yaw) * length, 0, Math.sin(yaw) * length}));
				}
				yaw = Math2.mod(yaw, 2 * Math.PI);
				if (yaw >= Math.PI / 4 && yaw < 3 * Math.PI / 4)
					return CollectionUtils.array(new Direction(new double[] {0, 0, length}));
				if (yaw >= 3 * Math.PI / 4 && yaw < 5 * Math.PI / 4)
					return CollectionUtils.array(new Direction(new double[] {-length, 0, 0}));
				if (yaw >= 5 * Math.PI / 4 && yaw < 7 * Math.PI / 4)
					return CollectionUtils.array(new Direction(new double[] {0, 0, -length}));
				assert yaw >= 0 && yaw < Math.PI / 4 || yaw >= 7 * Math.PI / 4 && yaw < 2 * Math.PI;
				return CollectionUtils.array(new Direction(new double[] {length, 0, 0}));
			} else if (object instanceof Location) {
				assert this.from != null;
				Location from = this.from.getSingle(event);
				if (from == null)
					return new Direction[0];
				Location to = (Location) object;
				return CollectionUtils.array(new Direction(to.toVector().subtract(from.toVector())));
			}
			assert false;
			return new Direction[0];
		} else {
			return CollectionUtils.array(new Direction(horizontal ? Direction.IGNORE_PITCH : 0, yaw, length));
		}
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Direction> getReturnType() {
		return Direction.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (amount != null ? amount.toString(event, debug) + " meter(s) " : "") +
				(direction != null ? Direction.toString(direction) : relativeTo != null ? " in " +
				(horizontal ? "horizontal " : "") +(facing ? "facing" : "direction") + " of " +
				relativeTo.toString(event, debug) : (horizontal ? "horizontally " : "") + Direction.toString(0, yaw, 1));
	}

}
