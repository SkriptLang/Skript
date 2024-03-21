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
import ch.njol.skript.lang.function.Function;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Arrays;
import java.util.Objects;

@Name("Function Result")
@Description("Returns the result of executing a function with the given arguments.")
@Examples("broadcast result of function {_function} with {_foo} and {_bar}")
@Since("INSERT VERSION")
public class ExprFunctionResult extends SimpleExpression<Object> {

	static {
		Skript.registerExpression(ExprFunctionResult.class, Object.class, ExpressionType.PATTERN_MATCHES_EVERYTHING,
				"[the] [call|execution] result of %functions% [(using|with) [[the] (argument|parameter)[s]] %-objects%]");
	}

	private Expression<Function<?>> functions;
	@Nullable
	private Expression<?> arguments;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		functions = (Expression<Function<?>>) exprs[0];
		if (exprs[1] == null)
			return true;
		arguments = LiteralUtils.defendExpression(exprs[1]);
		return true;
	}

	@Override
	@Nullable
	protected Object[] get(Event event) {
		return functions.stream(event)
			.map(function -> function.execute(event, arguments))
			.filter(Objects::nonNull)
			.flatMap(Arrays::stream)
			.toArray();
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<?> getReturnType() {
		return Object.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the result of " + functions.toString(event, debug) +
				(arguments == null ? "" : " with the arguments " + arguments.toString(event, debug));
	}

}
