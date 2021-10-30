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

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("unchecked")
public class EvtCommand extends SkriptEvent { // TODO condition to check whether a given command exists, & a conditon to check whether it's a custom skript command
	static {
		Skript.registerEvent("Command", EvtCommand.class, CollectionUtils.array(PlayerCommandPreprocessEvent.class, ServerCommandEvent.class), "command [%-string%]")
				.description("Called when a player enters a command (not necessarily a Skript command) but you can check if command is a skript command, see <a href='conditions.html#CondIsSkriptCommand'>Is a Skript command condition</a>.")
				.examples("on command:", "on command \"/stop\":", "on command \"pm Njol \":")
				.since("2.0");
	}
	
	@Nullable
	private String command = null;
	
	@SuppressWarnings("null")
	@Override
	public boolean init(final Literal<?>[] args, final int matchedPattern, final ParseResult parser) {
		if (args[0] != null) {
			command = ((Literal<String>) args[0]).getSingle();
			if (command.startsWith("/"))
				command = command.substring(1);
		}
		return true;
	}
	
	@SuppressWarnings("null")
	@Override
	public boolean check(final Event e) {
		if (command == null)
			return true;
		final String message;
		if (e instanceof PlayerCommandPreprocessEvent) {
			assert ((PlayerCommandPreprocessEvent) e).getMessage().startsWith("/");
			message = ((PlayerCommandPreprocessEvent) e).getMessage().substring(1);
		} else {
			message = ((ServerCommandEvent) e).getCommand();
		}
		return StringUtils.startsWithIgnoreCase(message, command)
				&& (command.contains(" ") || message.length() == command.length() || Character.isWhitespace(message.charAt(command.length()))); // if only the command is given, match that command only
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "command" + (command != null ? " /" + command : "");
	}
	
}
