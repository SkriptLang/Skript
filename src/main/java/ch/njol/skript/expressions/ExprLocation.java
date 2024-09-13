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
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.expressions.base.WrapperExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@Name("Location")
@Description("The location where an event happened (e.g. at an entity or block), or a location <a href='#ExprDirection'>relative</a> to another (e.g. 1 meter above another location).")
@Examples({"drop 5 apples at the event-location # exactly the same as writing 'drop 5 apples'",
		"set {_loc} to the location 1 meter above the player"})
@Since("2.0")
public class ExprLocation extends WrapperExpression<Location> {
	static {
		Skript.registerExpression(ExprLocation.class, Location.class, ExpressionType.SIMPLE, "[the] [event-](location|position)");
		Skript.registerExpression(ExprLocation.class, Location.class, ExpressionType.COMBINED, "[the] (location|position) %directions% [%location%]");
	}
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		if (exprs.length > 0) {
			super.setExpr(Direction.combine((Expression<? extends Direction>) exprs[0], (Expression<? extends Location>) exprs[1]));
			return true;
		} else {
			setExpr(new EventValueExpression<>(Location.class));
			return ((EventValueExpression<Location>) getExpr()).init();
		}
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return getExpr() instanceof EventValueExpression ? "the location" : "the location " + getExpr().toString(e, debug);
	}
	
}
