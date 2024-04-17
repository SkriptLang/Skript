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
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Clamp")
@Description("Clamps one or more values between two numbers.")
@Examples({
	"5 clamped between 0 and 10 # result = 5",
	"5.5 clamped between 0 and 5 # result = 5",
	"0.25 clamped between 0 and 0.5 # result = 0.25",
	"(5, 0, 10, 9, 13) clamped between 7 and 10 # result = (7, 7, 10, 9, 10)",
	"set {_clamped::*} to {_values::*} clamped between 0 and 10",
	"",
	"3 clamped below 5 # result = 3",
	"3.2 clamped below 2 # result = 2",
	"(-1, 3, 0.5, 9) clamped below 3 # result = (-1, 3, 0.5, 3)",
	"",
	"6.5 clamped above 6 # result = 6.5",
	"4 clamped above 5.5 # result = 5.5",
	"(3.14, 1, -9.8, 2.7) clamped above 2.5 # result = (3.14, 2.5, 2.5, 2.7)",
	"set {_not negative} to {_input} clamped above 0"
})
@Since("INSERT VERSION")
public class ExprClamp extends SimpleExpression<Number> {

	static {
		Skript.registerExpression(ExprClamp.class, Number.class, ExpressionType.COMBINED,
			"%numbers% clamped between %number% and %number%",
			"%numbers% clamped below %number%",
			"%numbers% clamped above %number%");
	}

	private enum Mode {
		BOTH {
			@Override
			public String toString() {
				return "between";
			}
		},
		BELOW {
			@Override
			public String toString() {
				return "below";
			}
		},
		ABOVE {
			@Override
			public String toString() {
				return "above";
			}
		}
	}

	private Expression<Number> values, minExpr, maxExpr;
	private Mode mode;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		mode = Mode.values()[matchedPattern];
		values = (Expression<Number>) expressions[0];
		if (mode == Mode.BOTH) {
			minExpr = (Expression<Number>) expressions[1];
			maxExpr = (Expression<Number>) expressions[2];
		} else if (mode == Mode.BELOW) {
			maxExpr = (Expression<Number>) expressions[1];
		} else if (mode == Mode.ABOVE) {
			minExpr = (Expression<Number>) expressions[1];
		}
		return true;
	}

	@Nullable
	@Override
	protected Number[] get(Event event) {
		Number[] numbers = values.getArray(event);
		Double[] clampedValues = new Double[numbers.length];
		double min = Double.NEGATIVE_INFINITY;
		double max = Double.POSITIVE_INFINITY;
		if (mode == Mode.ABOVE || mode == Mode.BOTH) {
			min = minExpr.getOptionalSingle(event).orElse(Double.NEGATIVE_INFINITY).doubleValue();
		}
		if (mode == Mode.BELOW || mode == Mode.BOTH) {
			max = maxExpr.getOptionalSingle(event).orElse(Double.POSITIVE_INFINITY).doubleValue();
		}
		// Make sure the min and max are in the correct order
		double trueMin = Math.min(min, max);
		double trueMax = Math.max(min, max);
		for (int i = 0; i < numbers.length; i++) {
			double value = numbers[i].doubleValue();
			clampedValues[i] = Math.max(Math.min(value, trueMax), trueMin);
		}
		return clampedValues;
	}

	@Override
	public boolean isSingle() {
		return values.isSingle();
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return values.toString(event, debug) + " clamped " + mode + " "
				+ ((mode == Mode.ABOVE || mode == Mode.BOTH) ? minExpr.toString(event, debug) : "")
				+ ((mode == Mode.BOTH) ? " and " : "")
				+ ((mode == Mode.BELOW || mode == Mode.BOTH) ? maxExpr.toString(event, debug) : "");
	}

}
