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

import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

@Name("Vectors - XYZ Component")
@Description("Gets or changes the x, y or z component of a vector.")
@Examples({
	"set {_v} to vector 1, 2, 3",
	"send \"%x of {_v}%, %y of {_v}%, %z of {_v}%\"",
	"add 1 to x of {_v}",
	"add 2 to y of {_v}",
	"add 3 to z of {_v}",
	"send \"%x of {_v}%, %y of {_v}%, %z of {_v}%\"",
	"set x component of {_v::*} to 1",
	"set y component of {_v::*} to 2",
	"set z component of {_v::*} to 3",
	"send \"%x component of {_v::*}%, %y component of {_v::*}%, %z component of {_v::*}%\""
})
@Since("2.2-dev28")
public class ExprVectorXYZ extends SimplePropertyExpression<Vector, Number> {
	
	static {
		// TODO: Combine with ExprCoordinates for 2.9
		register(ExprVectorXYZ.class, Number.class, "[vector] (0:x|1:y|2:z) [component[s]]", "vectors");
	}
	
	private final static Character[] axes = new Character[] {'x', 'y', 'z'};
	
	private int axis;
	
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		super.init(exprs, matchedPattern, isDelayed, parseResult);
		axis = parseResult.mark;
		return true;
	}
	
	@Override
	public Number convert(Vector vector) {
		return axis == 0 ? vector.getX() : (axis == 1 ? vector.getY() : vector.getZ());
	}
	
	@Override
	@SuppressWarnings("null")
	public Class<?>[] acceptChange(ChangeMode mode) {
		if ((mode == ChangeMode.ADD || mode == ChangeMode.REMOVE || mode == ChangeMode.SET)
				&& Changer.ChangerUtils.acceptsChange(getExpr(), ChangeMode.SET, Vector.class))
			return CollectionUtils.array(Number.class);
		return null;
	}
	
	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		assert delta != null;
		Vector[] vectors = getExpr().getArray(event);
		if (vectors.length == 0)
			return;
		double deltaValue = ((Number) delta[0]).doubleValue();
		switch (mode) {
			case REMOVE:
				deltaValue = -deltaValue;
				//$FALL-THROUGH$
			case ADD:
				for (Vector vector : vectors) {
					if (axis == 0)
						vector.setX(vector.getX() + deltaValue);
					else if (axis == 1)
						vector.setY(vector.getY() + deltaValue);
					else
						vector.setZ(vector.getZ() + deltaValue);
				}
				break;
			case SET:
				for (Vector vector : vectors) {
					if (axis == 0)
						vector.setX(deltaValue);
					else if (axis == 1)
						vector.setY(deltaValue);
					else
						vector.setZ(deltaValue);
				}
				break;
			default:
				assert false;
				return;
		}
		getExpr().change(event, vectors, ChangeMode.SET);
	}

	@Override
	public Class<Number> getReturnType() {
		return Number.class;
	}

	@Override
	protected String getPropertyName() {
		return axes[axis] + " component";
	}
}
