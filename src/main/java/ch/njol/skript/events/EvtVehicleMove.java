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
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.eclipse.jdt.annotation.Nullable;

public class EvtVehicleMove extends SkriptEvent {

	static {
		Skript.registerEvent("Vehicle Move", EvtVehicleMove.class, VehicleMoveEvent.class, "vehicle move", "mov(e|ing|) of [a] vehicle")
			.description("Called when a vehicle moves.",
				"NOTE: Vehicle move event will only be called when the vehicle moves position, not orientation (ie: looking around).",
				"NOTE: These events can be performance heavy as they are called quite often.",
				"If you use these events, and later remove them, a server restart is recommended to clear registered events from Skript.")
			.examples("on vehicle move:",
				"\tevent-entity is a player",
				"\tif entity does not have permission \"can.move.vehicle\":",
				"\t\tkill vehicle")
			.since("INSERT VERSION");
	}

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		return true;
	}

	@Override
	public boolean check(Event event) {
		VehicleMoveEvent e = (VehicleMoveEvent) event;
		return moveCheck(e.getFrom(), e.getTo());
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "vehicle move";
	}

	private static boolean moveCheck(Location from, Location to) {
		return from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ() || from.getWorld() != to.getWorld();
	}

}
