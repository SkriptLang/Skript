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
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Phill310
 */
@Name("Clamp")
@Description("Clamps one or more values between two numbers.")
@Examples({
	"clamp 5 between 0 and 10 = 5",
	"clamp 5.5 between 0 and 5 = 5",
	"clamp 0.25 between 0 and 0.5 = 0.25",
	"clamp 5 between 7 and 10 = 7",
	"clamp (5, 0, 10, 9, 13) between 7 and 10 = (7, 7, 10, 9, 10)",
	"",
	"set {_clamped::*} to clamp {_values::*} between 0 and 10"
})
@Since("INSERT VERSION")
public class ExprClamp extends SimpleExpression<Number> {

	static {
		Skript.registerExpression(ExprClamp.class, Number.class, ExpressionType.COMBINED,
			"clamp %numbers% between %number% and %number%");
	}

	private Expression<Number> values, min, max;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		values = (Expression<Number>) expressions[0];
		min = ((Expression<Number>) expressions[1]);
		max = (Expression<Number>) expressions[2];
		return true;
	}

	@Nullable
	@Override
	protected Number[] get(Event event) {
		Number[] numbers = values.getArray(event);
		Double[] clampedValues = new Double[numbers.length];
		double min = this.min.getSingle(event).doubleValue();
		double max = this.max.getSingle(event).doubleValue();
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
		return "clamp " + values.toString(event, debug) + " between " + min.toString(event, debug) + " and "
				+ max.toString(event, debug);
	}


}
