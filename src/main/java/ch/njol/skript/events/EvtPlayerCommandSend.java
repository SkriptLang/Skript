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
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;

@Name("Send Commands to Player")
@Description({
		"Called when the server sends a list of commands to the player. The sent commands can be modified via the <a href='expressions.html#ExprSentCommands'>sent commands expression</a>.",
		"Modifications will affect what commands show up for the player to tab complete. They will not affect what commands the player can actually run.",
		"Adding new commands to the list is illegal behavior and will be ignored."
})
@Examples({
		"on send commands to player:",
			"\tset sent commands to sent commands where [input does not contain \":\"]",
			"\tadd \"fly\" to sent commands"
})
@Since("INSERT VERSION")
public class EvtPlayerCommandSend extends SkriptEvent {

	static {
		Skript.registerEvent("Send Commands to Player", EvtPlayerCommandSend.class, PlayerCommandSendEvent.class, "send[ing] commands to player");
	}

	Collection<String> originalCommands = new ArrayList<>();

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, SkriptParser.ParseResult parseResult) {
		return true;
	}

	@Override
	public boolean check(Event event) {
		originalCommands.clear();
		originalCommands.addAll(((PlayerCommandSendEvent) event).getCommands());
		return true;
	}

	public ImmutableCollection<String> getOriginalCommands() {
		return ImmutableList.copyOf(originalCommands);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "send commands to player";
	}

}
