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

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.VariableString;
import ch.njol.skript.util.StringMode;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@Name("Command")
@Description("Executes a command. This can be useful to use other plugins in triggers.")
@Examples({"make player execute command \"/suicide\"",
		"execute console command \"/say Hello everyone!\""})
@Since("1.0")
public class EffCommand extends Effect {
	static {
		Skript.registerEffect(EffCommand.class,
				"[execute] [the] command %strings% [by %-commandsenders%]",
				"[execute] [the] %commandsenders% command %strings%",
				"(let|make) %commandsenders% execute [[the] command] %strings%");
	}
	
	@Nullable
	private Expression<CommandSender> senders;
	@SuppressWarnings("null")
	private Expression<String> commands;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
		if (matchedPattern == 0) {
			commands = (Expression<String>) vars[0];
			senders = (Expression<CommandSender>) vars[1];
		} else {
			senders = (Expression<CommandSender>) vars[0];
			commands = (Expression<String>) vars[1];
		}
		commands = VariableString.setStringMode(commands, StringMode.COMMAND);
		return true;
	}
	
	@Override
	public void execute(final Event event) {
		for (String command : commands.getArray(event)) {
			assert command != null;
			if (command.startsWith("/"))
				command = "" + command.substring(1);
			if (senders != null) {
				for (final CommandSender sender : senders.getArray(event)) {
					assert sender != null;
					Skript.dispatchCommand(sender, command);
				}
			} else {
				Skript.dispatchCommand(Bukkit.getConsoleSender(), command);
			}
		}
	}
	
	@Override
	public String toString(final @Nullable Event event, final boolean debug) {
		return "make " + (senders != null ? senders.toString(event, debug) : "the console") + " execute the command " + commands.toString(event, debug);
	}
	
}
