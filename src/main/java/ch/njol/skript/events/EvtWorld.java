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
import ch.njol.skript.lang.LiteralList;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.event.world.WorldEvent;
import org.jetbrains.annotations.Nullable;

public class EvtWorld extends SkriptEvent {

	static {
		// World Save Event
		Skript.registerEvent("World Save", EvtWorld.class, WorldSaveEvent.class, "world sav(e|ing) [of %-worlds%]")
				.description("Called when a world is saved to disk. Usually all worlds are saved simultaneously, but world management plugins could change this.")
				.examples(
					"on world save of \"world\":",
					"\tbroadcast \"The world %event-world% has been saved\"")
				.since("1.0, 2.8.0 (defining worlds)");

		// World Init Event
		Skript.registerEvent("World Init", EvtWorld.class, WorldInitEvent.class, "world init[ialization] [of %-worlds%]")
				.description("Called when a world is initialized. As all default worlds are initialized before",
					"any scripts are loaded, this event is only called for newly created worlds.",
					"World management plugins might change the behaviour of this event though.")
				.examples("on world init of \"world_the_end\":")
				.since("1.0, 2.8.0 (defining worlds)");

		// World Unload Event
		Skript.registerEvent("World Unload", EvtWorld.class, WorldUnloadEvent.class, "world unload[ing] [of %-worlds%]")
				.description("Called when a world is unloaded. This event will never be called if you don't have a world management plugin.")
				.examples(
					"on world unload:",
					"\tbroadcast \"the %event-world% has been unloaded!\"")
				.since("1.0, 2.8.0 (defining worlds)");

		// World Load Event
		Skript.registerEvent("World Load", EvtWorld.class, WorldLoadEvent.class, "world load[ing] [of %-worlds%]")
				.description("Called when a world is loaded. As with the world init event, this event will not be called for the server's default world(s).")
				.examples(
					"on world load of \"world_nether\":",
					"\tbroadcast \"The world %event-world% has been loaded!\"")
				.since("1.0, 2.8.0 (defining worlds)");
	}

	private Literal<World> worlds;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		worlds = (Literal<World>) args[0];
		if (worlds instanceof LiteralList<?> && worlds.getAnd()) {
			((LiteralList<World>) worlds).invertAnd();
		}
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (worlds == null)
			return true;
		World evtWorld = ((WorldEvent) event).getWorld();
		return worlds.check(event, world -> world.equals(evtWorld));
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "world save/init/unload/load" + (worlds == null ? "" : " of " + worlds.toString(event,debug));
	}

}
