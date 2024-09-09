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
package ch.njol.skript.effects;

import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.VectorMath;

@Name("Vectors - Rotate Around Vector")
@Description("Rotates one or more vectors around another vector")
@Examples("rotate {_v} around vector 1, 0, 0 by 90")
@Since("2.2-dev28")
public class EffVectorRotateAroundAnother extends Effect {

	static {
		Skript.registerEffect(EffVectorRotateAroundAnother.class, "rotate %vectors% around %vector% by %number% [degrees]");
	}
	
	@SuppressWarnings("null")
	private Expression<Vector> vectors, axis;

	@SuppressWarnings("null")
	private Expression<Number> degree;

	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean kleenean, ParseResult parseResult) {
		vectors = (Expression<Vector>) exprs[0];
		axis = (Expression<Vector>) exprs[1];
		degree = (Expression<Number>) exprs[2];
		return true;
	}

	@SuppressWarnings("null")
	@Override
	protected void execute(Event event) {
		Vector axis = this.axis.getSingle(event);
		Number angle = degree.getSingle(event);
		if (axis == null || angle == null)
			return;
		for (Vector vector : vectors.getArray(event))
			VectorMath.rot(vector, axis, angle.doubleValue());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "rotate " + vectors.toString(event, debug) + " around " + axis.toString(event, debug) + " by " + degree.toString(event, debug) + "degrees";
	}

}
