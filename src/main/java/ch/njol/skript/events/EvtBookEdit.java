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
import org.bukkit.event.player.PlayerEditBookEvent;
import org.jetbrains.annotations.Nullable;

public class EvtBookEdit extends SkriptEvent{
	
	static {
		Skript.registerEvent("Book Edit", EvtBookEdit.class, PlayerEditBookEvent.class, "book (edit|change|write)")
			.description("Called when a player edits a book.")
			.examples("on book edit:")
			.since("2.2-dev31");
	}
	
	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, SkriptParser.ParseResult parseResult) {
		return true;
	}
	
	@Override
	public boolean check(Event e) {
		if (!(e instanceof PlayerEditBookEvent)){
			return false;
		}
		return !((PlayerEditBookEvent) e).isSigning();
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "book edit";
	}
}
