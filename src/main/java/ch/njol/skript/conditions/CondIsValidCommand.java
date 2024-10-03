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

import static ch.njol.skript.command.Commands.scriptCommandExists;

import org.bukkit.Bukkit;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Is Valid Command")
@Description("Checks whether a command/string is a valid plugin command, or a custom Skript command.")
@Examples({
	"on command:",
		"\tif command is a valid command:",
			"\t\tsend \"%command% is a valid plugin command!\" to executor",
		"\tif command is a skript command:",
			"\t\tsend \"%command% is a custom Skript command!\" to executor",
})
@Since("2.6, INSERT VERSION (server command)")
public class CondIsValidCommand extends PropertyCondition<String> {
	
	static {
		register(CondIsValidCommand.class, PropertyType.BE, "[a[n]] [[:in]valid] [skript:s(k|c)ript] (command|cmd)", "strings");
	}

	private boolean invalid;
	private boolean skriptCommand;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		invalid = parseResult.hasTag("in");
		skriptCommand = parseResult.hasTag("skript");
		return super.init(expressions, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public boolean check(String cmd) {
		if (skriptCommand)
			return scriptCommandExists(cmd) ^ invalid;
		return Bukkit.getPluginCommand(cmd) != null ^ invalid;
	}
	
	@Override
	protected String getPropertyName() {
		return (invalid ? "invalid" : "valid") + (skriptCommand ? " skript" : " server") + " command";
	}
	
}
