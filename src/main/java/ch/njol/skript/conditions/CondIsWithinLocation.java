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

import org.bukkit.Location;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.AABB;
import ch.njol.util.Kleenean;

@Name("Is Within Location")
@Description({
	"Whether a location is within two other locations forming a cuboid.",
	"Using the <a href='conditions.html#CondCompare'>is between</a> condition will refer to a straight line between locations."
})
@Examples({
	"if player's location is within {_loc1} and {_loc2}:",
		"\tsend \"You are in a PvP zone!\" to player"
})
@Since("2.7")
public class CondIsWithinLocation extends Condition {

	static {
		PropertyCondition.register(CondIsWithinLocation.class, "within %location% and %location%", "locations");
	}

	private Expression<Location> locsToCheck, loc1, loc2;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setNegated(matchedPattern == 1);
		locsToCheck = (Expression<Location>) exprs[0];
		loc1 = (Expression<Location>) exprs[1];
		loc2 = (Expression<Location>) exprs[2];
		return true;
	}

	@Override
	public boolean check(Event event) {
		Location one = loc1.getSingle(event);
		Location two = loc2.getSingle(event);
		if (one == null || two == null || one.getWorld() != two.getWorld())
			return false;
		AABB box = new AABB(one, two);
		return locsToCheck.check(event, box::contains, isNegated());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return locsToCheck.toString(event, debug) + " is within " + loc1.toString(event, debug) + " and " + loc2.toString(event, debug);
	}

}
