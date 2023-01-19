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
import ch.njol.util.Math2;

/**
 * @author Peter Güttinger
 */
@Name("Random Number")
@Description({"A random number or integer between two given numbers. Use 'number' if you want any number with decimal parts, or use use 'integer' if you only want whole numbers.",
		"Please note that the order of the numbers doesn't matter, i.e. <code>random number between 2 and 1</code> will work as well as <code>random number between 1 and 2</code>."})
@Examples({"set the player's health to a random number between 5 and 10",
		"send \"You rolled a %random integer from 1 to 6%!\" to the player"})
@Since("1.4")
public class ExprRandomNumber extends SimpleExpression<Number> {
	static {
		Skript.registerExpression(ExprRandomNumber.class, Number.class, ExpressionType.COMBINED,
				"[a] random (1¦integer|2¦number) (from|between) %number% (to|and) %number%");
	}
	
	@SuppressWarnings("null")
	private Expression<? extends Number> lower, upper;
	
	private final Random rand = new Random();
	
	private boolean integer;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
		lower = (Expression<Number>) exprs[0];
		upper = (Expression<Number>) exprs[1];
		integer = parser.mark == 1;
		return true;
	}
	
	@Override
	@Nullable
	protected Number[] get(final Event event) {
		final Number l = lower.getSingle(event);
		final Number u = upper.getSingle(event);
		if (u == null || l == null)
			return null;
		final double ll = Math.min(l.doubleValue(), u.doubleValue());
		final double uu = Math.max(l.doubleValue(), u.doubleValue());
		if (integer) {
			return new Long[] {Math2.ceil(ll) + Math2.mod(rand.nextLong(), Math2.floor(uu) - Math2.ceil(ll) + 1)};
		} else {
			return new Double[] {ll + rand.nextDouble() * (uu - ll)};
		}
	}
	
	@Override
	public Class<? extends Number> getReturnType() {
		return integer ? Long.class : Double.class;
	}
	
	@Override
	public String toString(final @Nullable Event event, final boolean debug) {
		return "a random " + (integer ? "integer" : "number") + " between " + lower.toString(event, debug) + " and " + upper.toString(event, debug);
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
}
