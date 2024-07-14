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
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;


/**
 * @author bi0qaw
 */
@Name("Vectors - Location Vector Offset")
@Description("Returns the location offset by vectors.")
@Examples({"set {_loc} to {_loc} ~ {_v}"})
@Since("2.2-dev28")
public class ExprLocationVectorOffset extends SimpleExpression<Location> {

	static {
		Skript.registerExpression(ExprLocationVectorOffset.class, Location.class, ExpressionType.COMBINED,
				"%location% offset by [[the] vectors] %vectors%",
				"%location%[ ]~[~][ ]%vectors%");
	}

	@SuppressWarnings("null")
	private Expression<Location> location;

	@SuppressWarnings("null")
	private Expression<Vector> vectors;

	@Override
	@SuppressWarnings({"unchecked", "null"})
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		location = (Expression<Location>) exprs[0];
		vectors = (Expression<Vector>) exprs[1];
		return true;
	}

	@SuppressWarnings("null")
	@Override
	protected Location[] get(Event e) {
		Location l = location.getSingle(e);
		if (l == null)
			return null;
		Location clone = l.clone();
		for (Vector v : vectors.getArray(e))
			clone.add(v);
		return CollectionUtils.array(clone);
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Location> getReturnType() {
		return Location.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return location.toString() + " offset by " + vectors.toString();
	}

}
