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

import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

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
import ch.njol.util.VectorMath;
import ch.njol.util.coll.CollectionUtils;

@Name("Vectors - Spherical Shape")
@Description("Forms a 'spherical shaped' vector using yaw and pitch to manipulate the current point.")
@Examples({
	"loop 360 times:",
		"\tset {_v} to spherical vector radius 1, yaw loop-value, pitch loop-value",
	"set {_v} to spherical vector radius 1, yaw 45, pitch 90"
})
@Since("2.2-dev28")
public class ExprVectorSpherical extends SimpleExpression<Vector> {

	static {
		Skript.registerExpression(ExprVectorSpherical.class, Vector.class, ExpressionType.SIMPLE,
				"[a] [new] spherical vector [(from|with)] [radius] %number%, [yaw] %number%(,[ and]| and) [pitch] %number%");
	}

	@SuppressWarnings("null")
	private Expression<Number> radius, yaw, pitch;

	@Override
	@SuppressWarnings({"unchecked", "null"})
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		radius = (Expression<Number>) exprs[0];
		yaw = (Expression<Number>) exprs[1];
		pitch = (Expression<Number>) exprs[2];
		return true;
	}

	@Override
	@SuppressWarnings("null")
	protected Vector[] get(Event event) {
		Number radius = this.radius.getSingle(event);
		Number yaw = this.yaw.getSingle(event);
		Number pitch = this.pitch.getSingle(event);
		if (radius == null || yaw == null || pitch == null)
			return null;
		return CollectionUtils.array(VectorMath.fromSphericalCoordinates(radius.doubleValue(), VectorMath.fromSkriptYaw(yaw.floatValue()), pitch.floatValue() + 90));
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Vector> getReturnType() {
		return Vector.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "spherical vector with radius " + radius.toString(event, debug) + ", yaw " + yaw.toString(event, debug) +
				" and pitch" + pitch.toString(event, debug);
	}

}
