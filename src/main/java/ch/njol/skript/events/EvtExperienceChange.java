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
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.eclipse.jdt.annotation.Nullable;

public class EvtExperienceChange extends SkriptEvent {

	static {
		if (Skript.classExists("org.bukkit.event.player.PlayerExpChangeEvent")) {
			Skript.registerEvent("Experience Change", EvtExperienceChange.class, PlayerExpChangeEvent.class, "[player] level progress (change|update|:increase|:decrease)")
				.description("Called when a player's experience changes.")
				.examples(
					"on level progress change:",
						"\tset {_xp} to event-exp",
						"\tbroadcast \"%{_xp}%\""
				)
				.requiredPlugins("MC 1.14+")
				.since("INSERT VERSION");
		}
	}

	private static final int ANY = 0, UP = 1, DOWN = 2;
	private int mode = ANY;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		if (parseResult.hasTag("increase")) {
			mode = UP;
		} else if (parseResult.hasTag("decrease")) {
			mode = DOWN;
		}
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (mode == ANY)
			return true;
		PlayerExpChangeEvent expChangeEvent = (PlayerExpChangeEvent) event;
		return mode == UP ? expChangeEvent.getAmount() > 0 : expChangeEvent.getAmount() < 0;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "player level progress change " + (mode == UP ? "increase" : "decrease");
	}

}
