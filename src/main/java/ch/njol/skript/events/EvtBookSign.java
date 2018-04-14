/*
 * This file is part of Skript.
 *
 * Skript is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Skript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2011-2018 Peter Güttinger and contributors
 */
package ch.njol.skript.events;

import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser;

public class EvtBookSign extends SkriptEvent {

	static {
		Skript.registerEvent("Book Sign", EvtBookSign.class, PlayerEditBookEvent.class, "book sign[ing]")
				.description("Called when a player signs a book")
				.examples("")
				.since("2.2-dev31");
	}

	@Override
	public boolean init(final Literal<?>[] args, final int matchedPattern, final SkriptParser.ParseResult parseResult) {
		return true;
	}

	@Override
	public boolean check(Event e) {
		return e instanceof PlayerEditBookEvent && ((PlayerEditBookEvent) e).isSigning();
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "book sign";
	}
}
