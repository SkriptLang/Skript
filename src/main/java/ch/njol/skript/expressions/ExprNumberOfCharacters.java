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
import org.jetbrains.annotations.Nullable;

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

@Name("Number of Characters")
@Description("The number of uppercase, lowercase, or digit characters in a string.")
@Examples({"#Simple Chat Filter",
			"on chat:",
			"\tif number of uppercase chars in message / length of message > 0.5",
			"\t\tcancel event",
			"\t\tsend \"&lt;red&gt;Your message has to many caps!\" to player"})
@Since("2.5")
public class ExprNumberOfCharacters extends SimpleExpression<Long> {

	static {
		Skript.registerExpression(ExprNumberOfCharacters.class, Long.class, ExpressionType.SIMPLE,
				"number of upper[ ]case char(acters|s) in %string%",
				"number of lower[ ]case char(acters|s) in %string%",
				"number of digit char(acters|s) in %string%");
	}

	private int pattern = 0;

	@SuppressWarnings("null")
	private Expression<String> expr;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		pattern = matchedPattern;
		expr = (Expression<String>) exprs[0];
		return true;
	}

	@Override
	@SuppressWarnings("null")
	@Nullable
	protected Long[] get(Event e) {
		String str = expr.getSingle(e);
		if (str == null)
			return null;
		long size = 0;
		if (pattern == 0) {
			for (int c : (Iterable<Integer>) str.codePoints()::iterator) {
				if (Character.isUpperCase(c)) size++;
			}
		} else if (pattern == 1) {
			for (int c : (Iterable<Integer>) str.codePoints()::iterator) {
				if (Character.isLowerCase(c)) size++;
			}
		} else {
			for (int c : (Iterable<Integer>) str.codePoints()::iterator) {
				if (Character.isDigit(c)) size++;
			}
		}
		return new Long[]{size};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Long> getReturnType() {
		return Long.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		if (pattern == 0) {
			return "number of uppercase characters";
		} else if (pattern == 1) {
			return "number of lowercase characters";
		}
		return "number of digits";
	}

}
