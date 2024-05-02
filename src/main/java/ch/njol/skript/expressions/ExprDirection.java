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
import ch.njol.skript.util.Patterns;
import ch.njol.util.Kleenean;
import ch.njol.util.Math2;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.eclipse.jdt.annotation.Nullable;

@Name("Direction")
@Description("A helper expression for the <a href='classes.html#direction'>direction type</a>.")
@Examples({
	"thrust the player upwards",
	"set the block behind the player to water",
	"loop blocks above the player:",
	"\tset {_rand} to a random integer between 1 and 10",
	"\tset the block {_rand} meters south east of the loop-block to stone",
	"block in horizontal facing of the clicked entity from the player is air",
	"spawn a creeper 1.5 meters horizontally behind the player",
	"spawn a TNT 5 meters above and 2 meters horizontally behind the player",
	"thrust the last spawned TNT in the horizontal direction of the player with speed 0.2",
	"push the player upwards and horizontally forward at speed 0.5",
	"push the clicked entity in in the direction of the player at speed -0.5",
	"open the inventory of the block 2 blocks below the player to the player",
	"teleport the clicked entity behind the player",
	"grow a regular tree 2 meters horizontally behind the player"})
@Since("1.0 (basic), 2.0 (extended), INSERT VERSION (pattern changes)")
public class ExprDirection extends SimpleExpression<Direction> {

	private static final Patterns<BlockFace> PATTERNS = new Patterns<>(new Object[][]{
		// northeast, northwest, southeast and southwest need to be registered before normal usages otherwise `[-%direction%]` is given priority
		// naturally doesn't matter however this should help a bit with parsing "north west north west north west..."
		{"[%-number% [(block|meter|metre)[s]]] [to the] north[-| ]east[er(n|ly)|ward[s|ly]] [[side] of] [[and|,] %-direction%]", BlockFace.NORTH_EAST},
		{"[%-number% [(block|meter|metre)[s]]] [to the] north[-| ]west[er(n|ly)|ward[s|ly]] [[side] of] [[and|,] %-direction%]", BlockFace.NORTH_WEST},
		{"[%-number% [(block|meter|metre)[s]]] [to the] south[-| ]east[er(n|ly)|ward[s|ly]] [[side] of] [[and|,] %-direction%]", BlockFace.SOUTH_EAST},
		{"[%-number% [(block|meter|metre)[s]]] [to the] south[-| ]west[er(n|ly)|ward[s|ly]] [[side] of] [[and|,] %-direction%]", BlockFace.SOUTH_WEST},
		{"[%-number% [(block|meter|metre)[s]]] [to the] north[er(n|ly)|ward[s|ly]] [[side] of] [[and|,] %-direction%]", BlockFace.NORTH},
		{"[%-number% [(block|meter|metre)[s]]] [to the] east[er(n|ly)|ward[s|ly]] [[side] of] [[and|,] %-direction%]", BlockFace.EAST},
		{"[%-number% [(block|meter|metre)[s]]] [to the] south[er(n|ly)|ward[s|ly]] [[side] of] [[and|,] %-direction%]", BlockFace.SOUTH},
		{"[%-number% [(block|meter|metre)[s]]] [to the] west[er(n|ly)|ward[s|ly]] [[side] of] [[and|,] %-direction%]", BlockFace.WEST},
		{"[%-number% [(block|meter|metre)[s]]] (above|over|up[ward[s|ly]]) [%-direction%]", BlockFace.UP},
		{"[%-number% [(block|meter|metre)[s]]] (below|under|down[ward[s|ly]]) [%-direction%]", BlockFace.DOWN},
		{"[%-number% [(block|meter|metre)[s]]] [in [the]] [:horizontal] (direction|:facing) of %entity/block% [of|from]", BlockFace.SELF},
		{"[%-number% [(block|meter|metre)[s]]] [in] %entity/block%'[s] [:horizontal] (direction|:facing) [of|from]", BlockFace.SELF},
		{"[%-number% [(block|meter|metre)[s]]] [horizontal:horizontally] (in[ ]front [of]|forward[s])", BlockFace.SELF},
		{"[%-number% [(block|meter|metre)[s]]] [horizontal:horizontally] (2:(behind|backwards))", BlockFace.SELF},
		{"[%-number% [(block|meter|metre)[s]]] [horizontal:horizontally] [to the] (1:right) [[side] of]", BlockFace.SELF},
		{"[%-number% [(block|meter|metre)[s]]] [horizontal:horizontally] [to the] (-1:left) [[side] of]", BlockFace.SELF}
	});

	static {
		// TODO think about parsing statically & dynamically (also in general)
		// "at": see LitAt
		// TODO direction of %location% (from|relative to) %location%
		Skript.registerExpression(ExprDirection.class, Direction.class, ExpressionType.COMBINED, PATTERNS.getPatterns());
	}

	@Nullable
	public Expression<Number> amount;

	@Nullable
	private Vector directionVector;
	@Nullable
	private ExprDirection nextDirection;

	@Nullable
	private Expression<?> relativeTo;
	private boolean isHorizontal, isFacing;
	private double yaw;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		amount = (Expression<Number>) exprs[0];
		isHorizontal = parseResult.hasTag("horizontal");
		isFacing = parseResult.hasTag("facing");

		switch (matchedPattern) {
			case 10:
			case 11:
				relativeTo = exprs[1];
				break;
			case 12:
			case 13:
			case 14:
			case 15:
				yaw = Math.PI / 2 * parseResult.mark;
				break;
			default:
				BlockFace blockFace = PATTERNS.getInfo(matchedPattern);
				directionVector = blockFace.getDirection().clone();
				if (exprs[1] != null && (!(exprs[1] instanceof ExprDirection) || ((ExprDirection) exprs[1]).directionVector == null))
					return false;
				nextDirection = (ExprDirection) exprs[1];
				break;
		}
		return true;
	}

	@Override
	protected @Nullable Direction[] get(Event event) {
		Number numAmount = amount != null ? amount.getSingle(event) : 1;
		if (numAmount == null)
			return new Direction[0];
		double meterLength = numAmount.doubleValue();
		if (this.directionVector != null) {
			Vector directionVector = this.directionVector.multiply(meterLength);
			ExprDirection nextDirection = this.nextDirection;
			while (nextDirection != null) {
				numAmount = nextDirection.amount != null ? nextDirection.amount.getSingle(event) : 1;
				if (numAmount == null)
					return new Direction[0];
				assert nextDirection.directionVector != null; // this is checked in the init() method
				directionVector.add(nextDirection.directionVector.multiply(numAmount.doubleValue()));
				nextDirection = nextDirection.nextDirection;
			}
			return new Direction[]{new Direction(directionVector)};
		} else if (this.relativeTo != null) {
			Object relativeObject = relativeTo.getSingle(event);
			if (relativeObject == null)
				return new Direction[0];

			if (relativeObject instanceof Block) {
				BlockFace blockFace = Direction.getFacing((Block) relativeObject);
				if (blockFace == BlockFace.SELF || isHorizontal && (blockFace == BlockFace.UP || blockFace == BlockFace.DOWN))
					return new Direction[]{Direction.ZERO};
				return new Direction[]{new Direction(blockFace, meterLength)};
			} else if (relativeObject instanceof Entity) {
				Location relativeEntityLoc = ((Entity) relativeObject).getLocation();
				if (isHorizontal && isFacing) {
					double yaw = Direction.yawToRadians(relativeEntityLoc.getYaw());
					yaw = Math2.mod(yaw, 2 * Math.PI);
					if (yaw >= Math.PI / 4 && yaw < 3 * Math.PI / 4)
						return new Direction[]{new Direction(0, 0, meterLength)};
					if (yaw >= 3 * Math.PI / 4 && yaw < 5 * Math.PI / 4)
						return new Direction[]{new Direction(-meterLength, 0, 0)};
					if (yaw >= 5 * Math.PI / 4 && yaw < 7 * Math.PI / 4)
						return new Direction[]{new Direction(0, 0, -meterLength)};
					assert yaw >= 0 && yaw < Math.PI / 4 || yaw >= 7 * Math.PI / 4 && yaw < 2 * Math.PI;
					return new Direction[]{new Direction(meterLength, 0, 0)};
				} else if (isHorizontal) {
					return new Direction[] {new Direction(new double[] {Math.cos(yaw) * meterLength, 0, Math.sin(yaw) * meterLength})};
				} else if (isFacing) {
					double pitch = Direction.pitchToRadians(relativeEntityLoc.getPitch());
					assert pitch >= -Math.PI / 2 && pitch <= Math.PI / 2;
					if (pitch > Math.PI / 4)
						return new Direction[]{new Direction(new double[]{0, meterLength, 0})};
					if (pitch < -Math.PI / 4)
						return new Direction[]{new Direction(new double[]{0, -meterLength, 0})};
				}
				return new Direction[]{new Direction(relativeEntityLoc.getDirection().normalize().multiply(meterLength))};
			}
		} else {
			return new Direction[]{new Direction(isHorizontal ? Direction.IGNORE_PITCH : 0, yaw, meterLength)};
		}
		return new Direction[0];
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
		return String.format("%s%s%s%s",
			this.amount != null ? amount.toString(event, debug) + " meter(s) " : "",
			(isHorizontal ? "horizontally " : ""), (isFacing ? "facing " : ""),
			this.directionVector != null ? Direction.toString(directionVector) :
				this.relativeTo != null ? "of " + relativeTo.toString(event, debug) :
					Direction.toString(0, yaw, 1)
		);
	}

}
