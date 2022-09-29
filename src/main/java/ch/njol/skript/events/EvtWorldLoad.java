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
import org.bukkit.event.world.WorldLoadEvent;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Arrays;

public class EvtWorldLoad extends SkriptEvent {

	static {
		Skript.registerEvent("World Load", EvtWorldLoad.class, WorldLoadEvent.class, "world load[ing] [of %-worlds%]")
			.description("Called when a world is loaded. As with the world init event, this event will not be called for the server's default world(s).")
			.examples(
				"on world load of \"world_nether\":",
				"\tbroadcast \"The world %event-world% has been loaded!\"")
			.since("1.0, INSERT VERSION (defining worlds)");
	}

	@Nullable
	private World[] worlds;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		if (worlds != null)
			worlds = ((Literal<World>) args[0]).getArray();
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (worlds == null)
			return true;
		return Arrays.stream(worlds).anyMatch(world -> ((WorldLoadEvent) event).getWorld().equals(world));
	}


	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "loading of world" + (worlds == null ? "" : " " + worlds);
	}

}
