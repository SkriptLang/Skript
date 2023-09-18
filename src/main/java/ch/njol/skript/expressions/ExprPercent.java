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

@Name("Percent of")
@Description("Returns a percentage of one or more numbers.")
@Examples({
	"set damage to 10% of victim's health",
	"set damage to 125 percent of damage",
	"set {_result} to {_percent} percent of 999",
	"set {_result::*} to 10% of {_numbers::*}",
	"set experience to 50% of player's total experience"
})
@Since("INSERT VERSION")
public class ExprPercent extends SimpleExpression<Number> {

	static {
		Skript.registerExpression(ExprPercent.class, Number.class, ExpressionType.COMBINED, "%number%(\\%| percent) of %numbers%");
	}

	private Expression<Number> percent;
	private Expression<Number> numbers;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		percent = (Expression<Number>) exprs[0];
		numbers = (Expression<Number>) exprs[1];
		return true;
	}

	@Override
	protected @Nullable Number[] get(Event event) {
		Number percent = this.percent.getSingle(event);
		Number[] numbers = this.numbers.getArray(event);
		if (percent == null || numbers.length == 0)
			return null;

		Number[] results = new Number[numbers.length];
		for (int i = 0; i < numbers.length; i++) {
			results[i] = numbers[i].doubleValue() * percent.doubleValue() / 100;
		}

		return results;
	}

	@Override
	public boolean isSingle() {
		return numbers.isSingle();
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return percent.toString(event, debug) + " percent of " + numbers.toString(event, debug);
	}

}
