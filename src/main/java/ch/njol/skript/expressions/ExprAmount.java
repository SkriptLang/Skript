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
 *
 * Copyright 2011-2017 Peter Güttinger and contributors
 */
package ch.njol.skript.expressions;

import java.util.Map;
import java.util.TreeMap;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;

/**
 * TODO should 'amount of [item]' return the size of the stack?
 * 
 * @author Peter Güttinger
 */
@Name("Amount")
@Description({"The amount of something.",
		"Please note that <code>amount of %items%</code> will not return the number of items, but the number of stacks, e.g. 1 for a stack of 64 torches. To get the amount of items in a stack, see the <a href='#ExprItemAmount'>item amount</a> expression.",
		"Also please note that getting a list's recursive size can cause lag if the list is large."})
@Examples({"message \"There are %number of all players% players online!\""})
@Since("1.0")
public class ExprAmount extends SimpleExpression<Integer> {
	static {
		Skript.registerExpression(ExprAmount.class, Integer.class, ExpressionType.PROPERTY, 
				"(amount|number|size) of %objects%",
				"(recursive|total) (amount|number|size) of %objects%");
	}
	
	private String exprString = "";
	private boolean recursive = false;
	
	@SuppressWarnings("null")
	private Expression<?> expr;
	
	@SuppressWarnings("null")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		expr = exprs[0];
		if (expr instanceof Literal)
			return false;
		if (expr.isSingle()) {
			Skript.error("'" + expr.toString(null, false) + "' can only ever have one value at most, thus the 'amount of ...' expression is useless. Use '... exists' instead to find out whether the expression has a value.");
			return false;
		}
		if (matchedPattern == 1) {
			exprString = expr.toString();
			if (exprString.charAt(0) != '{') {
				Skript.error("Getting the recursive size of a list only applies to variables, thus the '" + exprString + "' expression is useless.");
				return false;
			}
			recursive = true;
		}
		return true;
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "amount of " + expr.toString(e, debug);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected Integer[] get(final Event e) {
		if (recursive) {
			boolean local = exprString.charAt(1) == '_';
			String substr = exprString.substring((local ? 2 : 1), exprString.length() - 1);
			assert substr != null;
			Object var = Variables.getVariable(substr, e, local);
			if (var != null && var instanceof TreeMap)
				return new Integer[] {getRecursiveSize((TreeMap<String, Object>) var)};
		}
		return new Integer[] {expr.getArray(e).length};
	}
	
	@SuppressWarnings("unchecked")
	private static int getRecursiveSize(TreeMap<String, Object> map) {
		int count = 0;
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			Object value = entry.getValue();
			if (value instanceof TreeMap)
				count += getRecursiveSize((TreeMap<String, Object>) value);
			else
				count++;
		}
		return count;
	}
	
}
