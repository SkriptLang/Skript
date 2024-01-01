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

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;

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
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		assert delta != null;
		final Vector[] vectors = getExpr().getArray(event);
		double deltaLength = ((Number) delta[0]).doubleValue();
		switch (mode) {
			case REMOVE:
				deltaLength = -deltaLength;
				//$FALL-THROUGH$
			case ADD:
				for (Vector vector : vectors) {
					if (deltaLength < 0 && vector.lengthSquared() < deltaLength * deltaLength) {
						vector.zero();
					} else {
						double newLength = deltaLength + vector.length();
						if (!vector.isNormalized())
							vector.normalize();
						vector.multiply(newLength);
					}
				}
				break;
			case SET:
				for (Vector vector : vectors) {
					if (deltaLength < 0) {
						vector.zero();
					} else {
						if (!vector.isNormalized())
							vector.normalize();
						vector.multiply(deltaLength);
					}
				}
				break;
		}
		getExpr().change(event, vectors, ChangeMode.SET);
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
