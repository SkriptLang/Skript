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
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.entity.EntityType;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityTeleportEvent;

import javax.annotation.Nullable;

public class EvtEntityTeleport extends SkriptEvent {

	static {
		Skript.registerEvent("Entity Teleport", EvtEntityTeleport.class, EntityTeleportEvent.class, "[any] entity teleport[ing] [of %entitytypes%]")
			.description("Called whenever a non-player entity is teleported, this event may also be called due to a result of natural causes, such as an Enderman or Shulker teleporting, or Wolfs teleporting to players.")
			.examples("on entity teleport:", "on entity teleport of creeper:")
			.since("VERSION");
	}

	@SuppressWarnings("unchecked")
	private Expression<EntityType> entities;;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, SkriptParser.ParseResult parseResult) {
		if (args.length > 0 && args[0] != null) {
			entities = (Expression<EntityType>) args[0];
		}
		return true;
	}


	@Override
	public boolean check(Event e) {
		if (e instanceof EntityTeleportEvent) {
			EntityTeleportEvent event = (EntityTeleportEvent) e;
			Entity entity = event.getEntity();
			if (entities != null) {
				for (EntityType entType : entities.getArray(e)) {
					if (entType.isInstance(entity)) {
						return true;
					}
				}
			} else {
				return true;
			}
		}
		return false;
	}


	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "on any entity teleport";
	}
}