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
import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.world.WorldUnloadEvent;
import org.eclipse.jdt.annotation.Nullable;

public class EvtWorldUnload extends SkriptEvent {

	static {
		Skript.registerEvent("World Unload", EvtWorldUnload.class, WorldUnloadEvent.class, "world unload[ing] [of %-worlds%]")
			.description("Called when a world is unloaded. This event will never be called if you don't have a world management plugin.")
			.examples(
				"on world unload:",
				"\tbroadcast \"the %event-world% has been unloaded!\"")
			.since("1.0, INSERT VERSION (defining worlds)");
	}

	@Nullable
	private Literal<World> worlds;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		worlds = (Literal<World>) args[0];
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (worlds == null)
			return true;

		return worlds.check(event, world -> world.equals(((WorldUnloadEvent) event).getWorld()));
	}


	@Override
	@Nullable
	public String toString(Event event, boolean debug) {
		return "unloading of world" + (worlds == null ? "" : " of " + worlds.toString(event,debug));
	}

}
