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

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;

@Name("Is a Skript command")
@Description("Checks whether a command/string is a custom Skript command.")
@Examples(
	{"# Example 1",
	"on command:", 
	"\tcommand is a skript command",
	"",
	"# Example 2",
	"\"sometext\" is a skript command"})
@Since("2.6")
public class CondIsSkriptCommand extends PropertyCondition<String> {
	
	static {
		register(CondIsSkriptCommand.class, PropertyType.BE, "[a] s(k|c)ript (command|cmd)", "string");
	}
	
	@Override
	public boolean check(String cmd) {
		return scriptCommandExists(cmd);
	}
	
	@Override
	protected String getPropertyName() {
		return "skript command";
	}
	
}
