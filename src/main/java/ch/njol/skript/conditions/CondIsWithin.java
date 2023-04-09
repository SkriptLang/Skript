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
package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.AABB;
import ch.njol.util.Kleenean;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.eclipse.jdt.annotation.Nullable;

@Name("Is Within")
@Description({
	"Whether a location is within something else. The \"something\" can be a block, an entity, a chunk, a world, " +
	"or a cuboid formed by two other locations.",
	"Note that using the <a href='conditions.html#CondCompare'>is between</a> condition will refer to a straight line " +
	"between locations, while this condition will refer to the cuboid between locations."
})
@Examples({
	"if player's location is within {_loc1} and {_loc2}:",
		"\tsend \"You are in a PvP zone!\" to player",
	"",
	"if player is in world(\"world\"):",
		"\tsend \"You are in the overworld!\" to player",
	"",
	"if attacker's location is inside of victim:",
		"\tcancel event",
		"\tsend \"Back up!\" to attacker and victim",
})
@Since("2.7")
@RequiredPlugins("MC 1.17+ (within block)")
public class CondIsWithin extends Condition {

	static {
		String validTypes = "entity/chunk/world";
		if (Skript.methodExists(Block.class, "getCollisionShape"))
			validTypes += "/block";

		Skript.registerCondition(CondIsWithin.class,
				"%locations% (is|are) within %location% and %location%",
				"%locations% (isn't|is not|aren't|are not) within %location% and %location%",
				"%locations% (is|are) (within|in[side [of]]) %" + validTypes + "%",
				"%locations% (isn't|is not|aren't|are not) (within|in[side [of]]) %" + validTypes + "%"
		);
	}

	private Expression<Location> locsToCheck, loc1, loc2;
	private Expression<?> area;
	private boolean withinLocations;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setNegated(matchedPattern % 2 == 1);
		locsToCheck = (Expression<Location>) exprs[0];
		if (matchedPattern <= 1) {
			// within two locations
			withinLocations = true;
			loc1 = (Expression<Location>) exprs[1];
			loc2 = (Expression<Location>) exprs[2];
		} else {
			// within an entity/block/chunk/world
			withinLocations = false;
			area = exprs[1];
		}
		return true;
	}

	@Override
	public boolean check(Event event) {
		// within two locations
		if (withinLocations) {
			Location one = loc1.getSingle(event);
			Location two = loc2.getSingle(event);
			if (one == null || two == null || one.getWorld() != two.getWorld())
				return false;
			AABB box = new AABB(one, two);
			return locsToCheck.check(event, box::contains, isNegated());
		}

		// else, within an entity/block/chunk/world
		Object area = this.area.getSingle(event);
		if (area == null)
			return false;

		// Entities
		if (area instanceof Entity) {
			BoundingBox box = ((Entity) area).getBoundingBox();
			return locsToCheck.check(event, (loc) -> box.contains(loc.toVector()), isNegated());
		}

		// Blocks
		if (area instanceof Block) {
			for (BoundingBox box : ((Block) area).getCollisionShape().getBoundingBoxes()) {
				// getCollisionShape().getBoundingBoxes() returns a list of bounding boxes relative to the block's position,
				// so we need to subtract the block position from each location
				Vector blockVector = ((Block) area).getLocation().toVector();
				if (!locsToCheck.check(event, (loc) -> box.contains(loc.toVector().subtract(blockVector)), isNegated())) {
					return false;
				}
			}
			// if all locations are within the block, return true
			return true;
		}

		// Chunks
		if (area instanceof Chunk) {
			return locsToCheck.check(event, (loc) -> loc.getChunk().equals(area), isNegated());
		}

		// Worlds
		if (area instanceof World) {
			return locsToCheck.check(event, (loc) -> loc.getWorld().equals(area), isNegated());
		}

		// fall-back
		return false;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		String str = locsToCheck.toString(event, debug) + " is within ";
		if (withinLocations) {
			str += loc1.toString(event, debug) + " and " + loc2.toString(event, debug);
		} else {
			str += area.toString(event, debug);
		}
		return str;
	}

}
