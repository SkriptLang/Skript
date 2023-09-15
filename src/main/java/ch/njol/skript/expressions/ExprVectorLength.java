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
package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author bi0qaw
 */
@Name("Vectors - Length")
@Description("Gets or sets the length of a vector.")
@Examples({"send \"%standard length of vector 1, 2, 3%\"",
	"set {_v} to vector 1, 2, 3",
	"set standard length of {_v} to 2",
	"send \"%standard length of {_v}%\""})
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
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		assert delta != null;
		final Vector v = getExpr().getSingle(e);
		if (v == null)
			return;
		double n = ((Number) delta[0]).doubleValue();
		switch (mode) {
			case ADD:
				if (n < 0 && v.lengthSquared() < n * n) {
					v.zero();
				} else {
					double l = n + v.length();
					v.normalize().multiply(l);
				}
				getExpr().change(e, new Vector[]{v}, ChangeMode.SET);
				break;
			case REMOVE:
				n = -n;
				//$FALL-THROUGH$
			case SET:
				if (n < 0)
					v.zero();
				else
					v.normalize().multiply(n);
				getExpr().change(e, new Vector[]{v}, ChangeMode.SET);
				break;
		}
	}

	@Override
	protected String getPropertyName() {
		return "vector length";
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

}
