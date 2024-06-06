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

import ch.njol.skript.Skript;
import ch.njol.skript.command.EffectCommandEvent;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

public class EvtEffectCommand extends SkriptEvent {

	static {
		Skript.registerEvent("Effect Command Event", EvtEffectCommand.class, EffectCommandEvent.class,
				"[executor:(1:console|2:player)] effect command")
				.description("Called when a player or console executes a skript effect command.")
				.examples(
						"on effect command:",
							"\tlog \"%sender%: %command%\" to file \"effectcommands.log\"")
				.since("INSERT VERSION");
	}

	private enum Executor {

		ANY("any"),
		CONSOLE("console"),
		PLAYER("player");

		final String toString;

		Executor(String toString) {
			this.toString = toString;
		}

		@Override
		public String toString() {
			return toString;
		}

	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Executor executor;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		executor = parseResult.hasTag("executor") ? Executor.values()[parseResult.mark] : Executor.ANY;
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (!(event instanceof EffectCommandEvent))
			return false;
		CommandSender sender = ((EffectCommandEvent) event).getSender();
		switch (executor) {
            case ANY:
				return true;
			case PLAYER:
				return sender instanceof Player;
			case CONSOLE:
				return sender instanceof ConsoleCommandSender;
        }
		return false;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (executor != Executor.ANY)
			return executor + " effect command";
		return "effect command";
	}

}
