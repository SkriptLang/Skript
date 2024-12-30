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
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

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
		double deltaValue = ((Number) delta[0]).doubleValue();
		Function<Vector, Vector> changeFunction;
		switch (mode) {
			case REMOVE:
				deltaValue = -deltaValue;
				//$FALL-THROUGH$
			case ADD:
				final double finalDeltaValue1 = deltaValue;
				changeFunction = vector -> {
					if (axis == 0) {
						vector.setX(vector.getX() + finalDeltaValue1);
					} else if (axis == 1) {
						vector.setY(vector.getY() + finalDeltaValue1);
					} else {
						vector.setZ(vector.getZ() + finalDeltaValue1);
					}
					return vector;
				};
				break;
			case SET:
				final double finalDeltaValue = deltaValue;
				changeFunction = vector -> {
					if (axis == 0) {
						vector.setX(finalDeltaValue);
					} else if (axis == 1) {
						vector.setY(finalDeltaValue);
					} else {
						vector.setZ(finalDeltaValue);
					}
					return vector;
				};
				break;
			default:
				assert false;
				return;
		}

		//noinspection unchecked,DataFlowIssue
		((Expression<Vector>) getExpr()).changeInPlace(event, changeFunction);
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
