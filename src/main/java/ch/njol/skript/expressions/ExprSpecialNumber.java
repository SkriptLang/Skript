/*
 * This file is part of Skript.
 *
 * Skript is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Skript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2011-2018 Peter Güttinger and contributors
 */
package ch.njol.skript.expressions;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

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

@Name("Special Number")
@Description("Special number values, namely NaN, Infinity and -Infinity")
@Examples({"if {_number} is NaN value:"})
@Since("@VERSION@")
public class ExprSpecialNumber extends SimpleExpression<Number> {

	private int value;

	static {
		Skript.registerExpression(ExprSpecialNumber.class, Number.class, ExpressionType.SIMPLE,
				"(0¦NaN|1¦[(2¦-|2¦minus)](infinity|\u221e)) value", // ∞ (infinity sign)
				"value of (0¦NaN|1¦[(2¦-|2¦minus)](infinity|\u221e))");
	}

	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final SkriptParser.ParseResult parseResult) {
		this.value = parseResult.mark;
		return true;
	}

	@Override
	protected Number[] get(final Event e) {
		return new Number[]{value == 0 ? Double.NaN : value == 1 ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return value == 0 ? "NaN value" : value == 1 ? "infinity value" : "-infinity value";
	}
}
