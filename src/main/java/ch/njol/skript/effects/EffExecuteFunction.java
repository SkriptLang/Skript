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
package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.function.Function;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Execute Function")
@Description("Executes a function with the given parameters.")
@Examples("run function \"rotate_%{_direction}%\" with {_structure-name}")
@Since("INSERT VERSION")
public class EffExecuteFunction extends Effect {

	static {
		Skript.registerEffect(EffExecuteFunction.class,
				"(call|execute|run) %functions% [(using|with) [[the] (argument|parameter)[s]] %-objects%]");
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
	protected void execute(Event event) {
		for (Function<?> function : functions.getArray(event))
			function.execute(event, arguments);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "execute " + functions.toString(event, debug) +
				(arguments == null ? "" : " with the arguments " + arguments.toString(event, debug));
	}

}
