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
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Function")
@Description("Returns a the function with the provided name")
@Examples("run (function \"foo\" from \"bar.sk\")")
@Since("INSERT VERSION")
public class ExprFunction extends SimpleExpression<Function> {

	static {
		Skript.registerExpression(ExprFunction.class, Function.class, ExpressionType.COMBINED,
				"[the] [:global] function[s] [named] %strings% [(in|from) [script] [file] %-string%]");
	}

	private Expression<String> names;
	@Nullable
	private Expression<String> script;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		names = (Expression<String>) exprs[0];
        script = (Expression<String>) exprs[1];
		if (script != null && parseResult.hasTag("global")) {
			Skript.error("A global function cannot be referenced from a specific script");
			return false;
		}
		return true;
	}

	@Override
	@Nullable
	protected Function<?>[] get(Event event) {
		String script = this.script == null ? null : this.script.getOptionalSingle(event)
			.map(ExprFunction::formatScript)
			.orElse(null);
		return names.stream(event)
			.map(name -> Functions.getFunction(name, script))
			.toArray(Function[]::new);
	}

	@Override
	public boolean isSingle() {
		return names.isSingle();
	}

	@Override
	public Class<? extends Function> getReturnType() {
		return Function.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the function" + (isSingle() ? " " : "s ") + names.toString(event, debug) +
				(script != null ? " from script " + script.toString(event, debug) : "");
	}

	private static String formatScript(String script) {
		if (!script.endsWith(".sk")) script += ".sk";
		script = script.replace('/', '\\');
		return script.charAt(0) == '\\' ? script.substring(1) : script;
	}
	
}
