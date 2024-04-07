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

import org.bukkit.command.CommandSender;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;

@Name("Command Sender")
@Description({
	"The player or the console who sent a command. Mostly useful in <a href='commands'>commands</a> and <a href='events.html#command'>command events</a>.",
	"If the command sender is a command block, its location can be retrieved by using %block's location%"
})
@Examples({
	"make the command sender execute \"/say hi!\"",
	"",
	"on command:",
		"\tlog \"%executor% used command /%command% %arguments%\" to \"commands.log\""
})
@Since("2.0")
@Events("command")
public class ExprCommandSender extends EventValueExpression<CommandSender> {

	static {
		register(ExprCommandSender.class, CommandSender.class, "[command['s]] (sender|executor)");
	}

	public ExprCommandSender() {
		super(CommandSender.class);
	}

}
