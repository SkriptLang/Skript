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
import org.bukkit.event.world.WorldInitEvent;
import org.eclipse.jdt.annotation.Nullable;

public class EvtWorldInit extends SkriptEvent {

	static {
		Skript.registerEvent("World Init", EvtWorldInit.class, WorldInitEvent.class, "world init[ialization] [of %-worlds%]")
			.description("Called when a world is initialised. As all default worlds are initialised before",
				"any scripts are loaded, this event is only called for newly created worlds.",
				"World management plugins might change the behaviour of this event though.")
			.examples("on world init of \"world_the_end\":")
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
		return worlds.check(event, world -> world.equals(((WorldInitEvent) event).getWorld()));
	}


	@Override
	@Nullable
	public String toString(Event event, boolean debug) {
		return "initialization of world" + (worlds == null ? "" : " of " + worlds.toString(event,debug));
	}

}
