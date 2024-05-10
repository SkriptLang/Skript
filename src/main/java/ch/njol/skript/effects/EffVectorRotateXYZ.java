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
import org.eclipse.jdt.annotation.Nullable;

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

@Name("Vectors - Rotate around XYZ")
@Description("Rotates one or more vectors around the x, y, or z axis by some amount of degrees")
@Examples({
	"rotate {_v} around x-axis by 90",
	"rotate {_v} around y-axis by 90",
	"rotate {_v} around z-axis by 90 degrees"
})
@Since("2.2-dev28")
public class EffVectorRotateXYZ extends Effect {

	static {
		Skript.registerEffect(EffVectorRotateXYZ.class, "rotate %vectors% around (0¦x|1¦y|2¦z)(-| )axis by %number% [degrees]");
	}

	private final static Character[] axes = new Character[] {'x', 'y', 'z'};

	@SuppressWarnings("null")
	private Expression<Vector> vectors;

	@SuppressWarnings("null")
	private Expression<Number> degree;
	private int axis;

	@Override
	@SuppressWarnings({"unchecked", "null"})
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		vectors = (Expression<Vector>) expressions[0];
		degree = (Expression<Number>) expressions[1];
		axis = parseResult.mark;
		return true;
	}

	@Override
	@SuppressWarnings("null")
	protected void execute(Event event) {
		Number angle = degree.getSingle(event);
		if (angle == null)
			return;
		switch (axis) {
			case 0:
				for (Vector vector : vectors.getArray(event))
					VectorMath.rotX(vector, angle.doubleValue());
				break;
			case 1:
				for (Vector vector : vectors.getArray(event))
					VectorMath.rotY(vector, angle.doubleValue());
				break;
			case 2:
				for (Vector vector : vectors.getArray(event))
					VectorMath.rotZ(vector, angle.doubleValue());
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "rotate " + vectors.toString(event, debug) + " around " + axes[axis] + "-axis" + " by " + degree.toString(event, debug) + "degrees";
	}

}
