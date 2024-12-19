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

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.util.VectorMath;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

@Name("Vectors - Length")
@Description("Gets or sets the length of a vector.")
@Examples({
	"send \"%standard length of vector 1, 2, 3%\"",
	"set {_v} to vector 1, 2, 3",
	"set standard length of {_v} to 2",
	"send \"%standard length of {_v}%\""
})
@Since("2.2-dev28")
public class ExprVectorLength extends SimplePropertyExpression<Vector, Number> {

	static {
		register(ExprVectorLength.class, Number.class, "(vector|standard|normal) length[s]", "vectors");
	}

	@Override
	@SuppressWarnings("unused")
	public Number convert(Vector vector) {
		return vector.length();
	}

	@Override
	@SuppressWarnings("null")
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.ADD || mode == ChangeMode.REMOVE || mode == ChangeMode.SET)
			return CollectionUtils.array(Number.class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		assert delta != null;
		double deltaLength = ((Number) delta[0]).doubleValue();

		Function<Vector, Vector> changeFunction;
		switch (mode) {
			case REMOVE:
				deltaLength = -deltaLength;
				//$FALL-THROUGH$
			case ADD:
				final double finalDeltaLength = deltaLength;
				final double finalDeltaLengthSquared = deltaLength * deltaLength;
				changeFunction = vector -> {
					if (VectorMath.isZero(vector) || (finalDeltaLength < 0 && vector.lengthSquared() < finalDeltaLengthSquared)) {
						vector.zero();
					} else {
						double newLength = finalDeltaLength + vector.length();
						if (!vector.isNormalized())
							vector.normalize();
						vector.multiply(newLength);
					}
					return vector;
				};
				break;
			case SET:
				final double finalDeltaLength1 = deltaLength;
				changeFunction = vector -> {
					if (finalDeltaLength1 < 0 || VectorMath.isZero(vector)) {
						vector.zero();
					} else {
						if (!vector.isNormalized())
							vector.normalize();
						vector.multiply(finalDeltaLength1);
					}
					return vector;
				};
				break;
			default:
				return;
		}

		//noinspection unchecked,DataFlowIssue
		((Expression<Vector>) getExpr()).changeInPlace(event, changeFunction);
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

	@Override
	protected String getPropertyName() {
		return "vector length";
	}

}
