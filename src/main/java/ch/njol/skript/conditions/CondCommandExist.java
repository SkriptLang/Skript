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
package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.command.Commands;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Command Exists")
@Description("Checks whether a command is registered with the server.")
@Examples({
	"# Example 1",
	"on command:",
	"\tthe command named command doesn't exist",
	"",
	"# Example 2",
	"the command \"sometext\" exist"})
@Since("INSERT VERSION")
public class CondCommandExist extends Condition {

	static {
		Skript.registerCondition(CondCommandExist.class,
			"[the] command[s] [named] %strings% exist[s]",
			"[the] command[s] [named] %strings% (doesn't|does not|do not|don't) exist[s]");
	}

	private Expression<String> commands;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		commands = (Expression<String>) exprs[0];
		setNegated(matchedPattern == 1);
		return true;
	}

	@Override
	public boolean check(Event event) {
		return commands.check(event, Commands::commandExists, isNegated());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "commands named " + commands + (isNegated() ? " don't " : "") + "exist";
	}
}
