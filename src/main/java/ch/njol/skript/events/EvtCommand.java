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
package ch.njol.skript.events;

import ch.njol.skript.command.Commands;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.StringUtils;
import ch.njol.util.coll.CollectionUtils;

@SuppressWarnings("unchecked")
public class EvtCommand extends SkriptEvent {
	static {
		Skript.registerEvent("Command", EvtCommand.class, CollectionUtils.array(PlayerCommandPreprocessEvent.class, ServerCommandEvent.class), "[1:existing] command [%-string%]")
				.description("Called when a player enters a command (not necessarily a Skript command) but you can check if command is a skript command, see <a href='conditions.html#CondIsSkriptCommand'>Is a Skript command condition</a>.")
				.examples("on command:", "on command \"/stop\":", "on command \"pm Njol \":", "on existing command:")
				.since("2.0, INSERT VERSION (existing only)");
	}

	@Nullable
	private String command = null;
	private Boolean existOnly;

	@Override
	@SuppressWarnings("null")
	public boolean init(final Literal<?>[] args, final int matchedPattern, final ParseResult parser) {
		if (args[0] != null) {
			command = ((Literal<String>) args[0]).getSingle();
			if (command.startsWith("/"))
				command = command.substring(1);
		}
		existOnly = parser.mark == 1;
		return true;
	}

	public boolean check(Event event) {
		if (event instanceof ServerCommandEvent && ((ServerCommandEvent) event).getCommand().isEmpty())
			return false;

		if (command == null) {
			if (existOnly && event instanceof ServerCommandEvent && Commands.getCommandMap().getCommand(((ServerCommandEvent) event).getCommand()) == null)
				return false;
			if (existOnly && event instanceof PlayerCommandPreprocessEvent && Commands.getCommandMap().getCommand(((PlayerCommandPreprocessEvent) event).getMessage().substring(1)) == null)
				return false;
			return true;
		}

		String message;
		if (event instanceof ServerCommandEvent) {
			message = ((ServerCommandEvent) event).getCommand();
		} else {
			assert ((PlayerCommandPreprocessEvent) event).getMessage().startsWith("/");
			message = ((PlayerCommandPreprocessEvent) event).getMessage().substring(1);
		}
		return StringUtils.startsWithIgnoreCase(message, command)
			&& (command.contains(" ") || message.length() == command.length() || Character.isWhitespace(message.charAt(command.length())));
	}
	
	@Override
	@Nullable
	public String toString(final Event event, final boolean debug) {
		return "command" + (command != null ? " /" + command : "");
	}
	
}
