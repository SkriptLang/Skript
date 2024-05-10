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
import ch.njol.util.VectorMath;
import ch.njol.util.coll.CollectionUtils;

@Name("Vectors - Vector from Pitch and Yaw")
@Description("Creates a vector from a yaw and pitch value.")
@Examples("set {_v} to vector from yaw 45 and pitch 45")
@Since("2.2-dev28")
public class ExprVectorFromYawAndPitch extends SimpleExpression<Vector> {

	static {
		Skript.registerExpression(ExprVectorFromYawAndPitch.class, Vector.class, ExpressionType.COMBINED,
				"[a] [new] vector (from|with) yaw %number% and pitch %number%");
	}

	@SuppressWarnings("null")
	private Expression<Number> pitch, yaw;

	@Override
	@SuppressWarnings({"unchecked", "null"})
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		yaw = (Expression<Number>) exprs[0];
		pitch = (Expression<Number>) exprs[1];
		return true;
	}

	@Override
	@SuppressWarnings("null")
	protected Vector[] get(Event event) {
		Number skriptYaw = yaw.getSingle(event);
		Number skriptPitch = pitch.getSingle(event);
		if (skriptYaw == null || skriptPitch == null)
			return null;
		float yaw = VectorMath.fromSkriptYaw(VectorMath.wrapAngleDeg(skriptYaw.floatValue()));
		float pitch = VectorMath.fromSkriptPitch(VectorMath.wrapAngleDeg(skriptPitch.floatValue()));
		return CollectionUtils.array(VectorMath.fromYawAndPitch(yaw, pitch));
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
		return "vector from yaw " + yaw.toString(event, debug) + " and pitch " + pitch.toString(event, debug);
	}

}
