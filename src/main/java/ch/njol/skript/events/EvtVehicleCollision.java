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
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.event.vehicle.VehicleBlockCollisionEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.eclipse.jdt.annotation.Nullable;

public class EvtVehicleCollision extends SkriptEvent {

	static {
		Class<? extends Event>[] events = CollectionUtils.array(VehicleBlockCollisionEvent.class, VehicleEntityCollisionEvent.class);

		Skript.registerEvent("Vehicle Collision", EvtVehicleCollision.class, events, "vehicle [(1¦block|2¦entity)] collision", "(1¦block|2¦entity|) collision of [a] vehicle")
			.description("Called when a vehicle collides with an entity or a block.")
			.examples("on vehicle move:",
				"\t\tpush vehicle upwards with force 1.5")
			.since("INSERT VERSION");
	}

	private boolean isAny, isBlock, isEntity;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		isAny = parseResult.mark == 0;
		isBlock = parseResult.mark == 1;
		isEntity = parseResult.mark == 2;
		return true;
	}

	@Override
	public boolean check(Event e) {
		return isEntity ? e instanceof VehicleEntityCollisionEvent : (isBlock ? e instanceof VehicleBlockCollisionEvent : true);
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "vehicle " + (isEntity ? "entity " : (isAny ? "" : "block ")) + "collision";
	}

}
