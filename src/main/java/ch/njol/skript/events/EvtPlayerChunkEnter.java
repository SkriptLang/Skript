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
import ch.njol.skript.lang.SkriptParser.ParseResult;

import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;

import org.eclipse.jdt.annotation.Nullable;

public class EvtPlayerChunkEnter extends SkriptEvent {

	static {
		Skript.registerEvent("Player Chunk Enter", EvtPlayerChunkEnter.class, PlayerMoveEvent.class, "[player] (enter[s] [a] chunk|chunk enter[ing])")
				.description("Called when a player enters a chunk. Note that this event is based on 'player move' event, and may be called frequent internally.")
				.examples(
						"on player enters a chunk:",
						"\tsend \"You entered a chunk: %past event-chunk% -> %event-chunk%!\" to player"
				).since("2.7");
	}

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		return true;
	}

	@Override
	public boolean check(Event event) {
		return ((PlayerMoveEvent) event).getFrom().getChunk() != ((PlayerMoveEvent) event).getTo().getChunk();
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "player enter chunk";
	}

}
