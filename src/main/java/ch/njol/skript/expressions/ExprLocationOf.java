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
import ch.njol.skript.expressions.base.WrapperExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@Name("Location")
@Description({"The location of a block or entity. This not only represents the x, y and z coordinates of the location but also includes the world and the direction an entity is looking " +
		"(e.g. teleporting to a saved location will make the teleported entity face the same saved direction every time).",
		"Please note that the location of an entity is at it's feet, use <a href='#ExprEyeLocation'>head location</a> to get the location of the head."})
@Examples({"set {home::%uuid of player%} to the location of the player",
		"message \"You home was set to %player's location% in %player's world%.\""})
@Since("")
public class ExprLocationOf extends WrapperExpression<Location> {
	static {
		Skript.registerExpression(ExprLocationOf.class, Location.class, ExpressionType.PROPERTY, "(location|position) of %location%", "%location%'[s] (location|position)");
	}
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		setExpr((Expression<? extends Location>) exprs[0]);
		return true;
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "the location of " + getExpr().toString(e, debug);
	}
	
}
