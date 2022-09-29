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
import org.bukkit.event.world.WorldSaveEvent;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Arrays;

public class EvtWorldSave extends SkriptEvent {

	static {
		Skript.registerEvent("World Save", EvtWorldSave.class, WorldSaveEvent.class, "world sav(e|ing) [of %-worlds%]")
			.description("Called when a world is saved to disk. Usually all worlds are saved simultaneously, but world management plugins could change this.")
			.examples(
				"on world save of \"world\":",
				"\tbroadcast \"The world %event-world% has been saved\"")
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
		return Arrays.stream(worlds).anyMatch(world -> ((WorldSaveEvent) event).getWorld().equals(world));
	}


	@Override
	public String toString(final @Nullable Event event, final boolean debug) {
		return "saving of world" + (worlds == null ? "" : " " + worlds);
	}

}
