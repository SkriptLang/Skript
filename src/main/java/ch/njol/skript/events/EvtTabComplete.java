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
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser;
import org.bukkit.event.Event;
import org.bukkit.event.server.TabCompleteEvent;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Arrays;

public class EvtTabComplete extends SkriptEvent {

	static {
		Skript.registerEvent("Tab Complete", EvtTabComplete.class, TabCompleteEvent.class, "tab complete [(of|for) %-strings%]")
			.description("Called when a player attempts to tab complete any command.")
			.examples("on tab complete of \"test\":",
				"\ttab argument 1 is \"bar\"",
				"\tset tab completions for position 2 to \"foo1\",\"foo2\", and \"foo3\"")
			.since("INSERT VERSION");
	}

	@Nullable
	private String[] commands;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, SkriptParser.ParseResult parseResult) {
		commands = args[0] == null ? null : ((Literal<String>) args[0]).getArray();
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (commands == null)
			return true;
		TabCompleteEvent tabEvent = (TabCompleteEvent) event;
		String command  = tabEvent.getBuffer().split(" ")[0];
		if (command.charAt(0) == '/')
			command = command.substring(1);
		for (String s : commands) {
			if (s.charAt(0) == '/')
				s = s.substring(1);
			if (s.equalsIgnoreCase(command))
				return true;
		}
		return false;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "tab complete" + (commands == null ? "" : " for " + Arrays.toString(commands));
	}
}
