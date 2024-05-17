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

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.StringUtils;
import ch.njol.util.coll.CollectionUtils;

public class EvtCommand extends SkriptEvent {

	static {
		Skript.registerEvent("Command", EvtCommand.class, CollectionUtils.array(PlayerCommandPreprocessEvent.class, ServerCommandEvent.class),
				"[1:console|2:player] command [%-string%]")
				.description(
						"Called when a player or the console enters a command (not necessarily a Skript command)" +
						"but you can check if command is a skript command, see <a href='conditions.html#CondIsValidCommand'>Is Valid Command condition</a>.")
				.examples(
						"on command:",
						"on command \"/stop\":",
						"on command \"pm Njol\":",
						"on console command:",
						"on player command \"/op\"")
				.since("2.0, INSERT VERSION (specific sender)");
	}

	private enum Sender {

		ANY(""),
		CONSOLE("console"),
		PLAYER("player");

		final String toString;

		Sender(String toString) {
			this.toString = toString;
		}

		static Sender valueOf(CommandSender sender) {
			if (sender instanceof ConsoleCommandSender) {
				return CONSOLE;
			} else if (sender instanceof Player) {
				return PLAYER;
			} else {
				return ANY;
			}
		}

		@Override
		public String toString() {
			return toString;
		}

	}

	@Nullable
	private String command;
	@SuppressWarnings("NotNullFieldNotInitialized")
	private Sender sender;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		if (args[0] != null) {
			command = ((Literal<String>) args[0]).getSingle();
			if (command.startsWith("/"))
				command = command.substring(1);
		}
		sender = Sender.values()[parseResult.mark];
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (event instanceof ServerCommandEvent) {
			String command = ((ServerCommandEvent) event).getCommand();
			if (command.isEmpty() || command.startsWith(SkriptConfig.effectCommandToken.value()))
				return false;
		}
		Sender eventSender = Sender.valueOf(getSender(event));
		if (command == null) {
			if (sender != Sender.ANY) {
				return eventSender == sender;
			}
			return true;
		}
		String message;
		if (event instanceof PlayerCommandPreprocessEvent) {
			assert ((PlayerCommandPreprocessEvent) event).getMessage().startsWith("/");
			message = ((PlayerCommandPreprocessEvent) event).getMessage().substring(1);
		} else {
			message = ((ServerCommandEvent) event).getCommand();
		}
		// if only the command is given, match that command only
		if (StringUtils.startsWithIgnoreCase(message, command)
			&& (command.contains(" ") || message.length() == command.length()
			|| Character.isWhitespace(message.charAt(command.length())))) {
			if (sender != Sender.ANY) {
				return eventSender == sender;
			}
			return true;
		}
		return false;
	}
	
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		String sender = "";
		if (this.sender != Sender.ANY)
			sender = this.sender + " ";
		return sender + "command" + (command != null ? " /" + command : "");
	}

	private static CommandSender getSender(Event event) {
		if (event instanceof PlayerCommandPreprocessEvent) {
			return ((PlayerCommandPreprocessEvent) event).getPlayer();
		} else if (event instanceof ServerCommandEvent) {
			return ((ServerCommandEvent) event).getSender();
		} else {
			assert false;
			return null;
		}
	}

}
