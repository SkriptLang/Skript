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

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.ApiStatus;

@Name("Vectors - Rotate Around Vector")
@Description("Rotates one or more vectors around another vector")
@Examples("rotate {_v} around vector 1, 0, 0 by 90")
@Since("2.2-dev28")
public class EffVectorRotateAroundAnother extends Effect {

	private static final double DEG_TO_RAD = Math.PI / 180;

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
			rot(vector, axis, angle.doubleValue());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "rotate " + vectors.toString(event, debug) + " around " + axis.toString(event, debug) + " by " + degree.toString(event, debug) + "degrees";
	}

	// TODO Mark as private next version after VectorMath deletion
	@ApiStatus.Internal
	public static Vector rot(Vector vector, Vector axis, double angle) {
		double sin = Math.sin(angle * DEG_TO_RAD);
		double cos = Math.cos(angle * DEG_TO_RAD);
		Vector a = axis.clone().normalize();
		double ax = a.getX();
		double ay = a.getY();
		double az = a.getZ();
		Vector rotx = new Vector(cos+ax*ax*(1-cos), ax*ay*(1-cos)-az*sin, ax*az*(1-cos)+ay*sin);
		Vector roty = new Vector(ay*ax*(1-cos)+az*sin, cos+ay*ay*(1-cos), ay*az*(1-cos)-ax*sin);
		Vector rotz = new Vector(az*ax*(1-cos)-ay*sin, az*ay*(1-cos)+ax*sin, cos+az*az*(1-cos));
		double x = rotx.dot(vector);
		double y = roty.dot(vector);
		double z = rotz.dot(vector);
		vector.setX(x).setY(y).setZ(z);
		return vector;
	}

}
