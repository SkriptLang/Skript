/**
 * This file is part of Skript.
 * <p>
 * Skript is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Skript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.effects;

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
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author bi0qaw
 */
@Name("Vectors - Rotate Around Vector")
@Description("Rotates a vector around another vector")
@Examples({"rotate {_v} around vector 1, 0, 0 by 90"})
@Since("2.2-dev28")
public class EffVectorRotateAroundAnother extends Effect {

	static {
		Skript.registerEffect(EffVectorRotateAroundAnother.class, "rotate %vectors% around %vector% by %number% [degrees]");
	}

	@SuppressWarnings("null")
	private Expression<Vector> first, second;

	@SuppressWarnings("null")
	private Expression<Number> degree;

	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean kleenean, ParseResult parseResult) {
		first = (Expression<Vector>) exprs[0];
		second = (Expression<Vector>) exprs[1];
		degree = (Expression<Number>) exprs[2];
		return true;
	}

	@SuppressWarnings("null")
	@Override
	protected void execute(Event e) {
		Vector v2 = second.getSingle(e);
		Number d = degree.getSingle(e);
		if (v2 == null || d == null)
			return;
		for (Vector v1 : first.getArray(e))
			VectorMath.rot(v1, v2, d.doubleValue());
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "rotate " + first.toString(e, debug) + " around " + second.toString(e, debug) + " by " + degree + "degrees";
	}

}
