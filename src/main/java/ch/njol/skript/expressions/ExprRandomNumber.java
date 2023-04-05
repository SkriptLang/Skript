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

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import ch.njol.util.Math2;
import org.bukkit.event.Event;
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

@Name("Random Number")
@Description({
	"A random number or integer between two given numbers. Use 'number' if you want any number with decimal parts, or use use 'integer' if you only want whole numbers.",
	"Please note that the order of the numbers doesn't matter, i.e. <code>random number between 2 and 1</code> will work as well as <code>random number between 1 and 2</code>."
})
@Examples({
	"set the player's health to a random number between 5 and 10",
	"send \"You rolled a %random integer from 1 to 6%!\" to the player"
})
@Since("1.4")
public class ExprRandomNumber extends SimpleExpression<Number> {

	static {
		Skript.registerExpression(ExprRandomNumber.class, Number.class, ExpressionType.COMBINED,
				"[a] random (:integer|number) (from|between) %number% (to|and) %number%");
	}

	private final Random random = ThreadLocalRandom.current();
	private Expression<Number> from, to;
	private boolean isInteger;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
		from = (Expression<Number>) exprs[0];
		to = (Expression<Number>) exprs[1];
		isInteger = parser.hasTag("integer");
		return true;
	}

	@Override
	@Nullable
	protected Number[] get(Event event) {
		Number from = this.from.getSingle(event);
		Number to = this.to.getSingle(event);

		if (to == null || from == null)
			return new Number[0];

		double min = Math.min(from.doubleValue(), to.doubleValue());
		double max = Math.max(from.doubleValue(), to.doubleValue());

		if (isInteger) {
			if (max - min < 1)
				return new Long[0];
			return new Long[] {random.nextLong(Math2.ceil(min), Math2.floor(max) + 1)};
		} else {
			return new Double[] {min + random.nextDouble() * (max - min)};
		}
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return isInteger ? Long.class : Double.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "a random " + (isInteger ? "integer" : "number") + " between " + from.toString(event, debug) + " and " + to.toString(event, debug);
	}

	@Override
	public boolean isSingle() {
		return true;
	}

}
