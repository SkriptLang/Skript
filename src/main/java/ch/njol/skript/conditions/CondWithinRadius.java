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
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Is Within Radius")
@Description("Checks whether a location is within a certain radius of another location.")
@Examples({
	"on damage:",
	"\tif attacker's location is within 10 blocks around {_spawn}:",
	"\t\tcancel event",
	"\t\tsend \"You can't PVP in spawn.\""
})
@Since("INSERT VERSION")
public class CondWithinRadius extends Condition {

	static {
		Skript.registerCondition(CondWithinRadius.class,
			"%locations% (is|are) within %number% (block|metre|meter)[s] (around|of) %location%",
			"%locations% (isn't|is not|aren't|are not) within %number% (block|metre|meter)[s] (around|of) %location%");
	}

	private Expression<Location> locations;
	private Expression<Number> radius;
	private Expression<Location> point;


	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		locations = (Expression<Location>) exprs[0];
		radius = (Expression<Number>) exprs[1];
		point = (Expression<Location>) exprs[2];
		setNegated(matchedPattern == 1);
		return true;
	}

	@Override
	public boolean check(Event event) {
		Number radius = this.radius.getSingle(event);
		Location center = point.getSingle(event);
		if (radius == null || center == null)
			return false;
		return locations.check(event, location -> {
			if (!location.getWorld().equals(center.getWorld()))
				return false;
			return location.distance(center) <= radius.doubleValue();
		}, isNegated());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return locations.toString(event, debug) + (locations.isSingle() ? " is " : " are ") + (isNegated() ? " not " : "")
			+ "within " + radius.toString(event, debug) + " blocks around " + point.toString(event, debug);
	}

}
