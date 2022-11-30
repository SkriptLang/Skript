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

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.Location;
import org.bukkit.WorldBorder;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Is Inside World Border")
@Description("Checks if a location is inside a world border.")
@Examples({
	"if player's location is not inside {_border}:",
		"\tsend \"Get inside quick!\" to player"
})
@Since("INSERT VERSION")
public class CondIsInsideWorldBorder extends Condition {

	static {
		PropertyCondition.register(CondIsInsideWorldBorder.class, "inside %worldborders%", "locations");
	}

	private Expression<Location> location;
	private Expression<WorldBorder> worldBorders;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		location = (Expression<Location>) exprs[0];
		worldBorders = (Expression<WorldBorder>) exprs[1];
		setNegated(matchedPattern == 1);
		return true;
	}

	@Override
	public boolean check(Event event) {
		return location.check(event, location -> worldBorders.check(event, worldBorder -> worldBorder.isInside(location)), isNegated());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return location.toString(event, debug) + (location.isSingle() ? " is " : " are ") + "inside " + worldBorders.toString(event, debug);
	}

}
