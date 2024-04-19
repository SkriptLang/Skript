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
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.entity.EntityType;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import javax.annotation.Nullable;

public class EvtTeleport extends SkriptEvent {

	static {
		Skript.registerEvent("Teleport", EvtTeleport.class, CollectionUtils.array(EntityTeleportEvent.class, PlayerTeleportEvent.class), "[%entitytypes%] teleport[ing]")
			.description(
				"This event can be used to listen to teleports from non-players or player entities respectively.",
				"When teleporting entities, the event may also be called due to a result of natural causes, such as an enderman or shulker teleporting, or wolves teleporting to players.",
				"When teleporting players, the event can be called by teleporting through a nether/end portal, or by other means (e.g. plugins).")
			.examples(
				"on teleport:",
				"on player teleport:",
				"on creeper teleport:"
			)
			.since("1.0, INSERT VERSION (entity teleport)");
	}

	@SuppressWarnings("unchecked")
	private Expression<EntityType> entities;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		if (args[0] != null) {
			entities = (Expression<EntityType>) args[0];
		}
		return true;
	}


	@Override
	public boolean check(Event event) {
		if (event instanceof EntityTeleportEvent) {
			Entity entity = ((EntityTeleportEvent) event).getEntity();
			return checkEntity(entity, event);
		} else if (event instanceof PlayerTeleportEvent) {
			Entity entity = ((PlayerTeleportEvent) event).getPlayer();
			return checkEntity(entity, event);
		} else {
			return false;
		}
	}

	private boolean checkEntity(Entity entity, Event event) {
		if (entities != null) {
			for (EntityType entType : entities.getAll(event)) {
				if (entType.isInstance(entity)) {
					return true;
				}
			}
		} else {
			return true;
		}
		return false;
	}

	public String toString(@Nullable Event e, boolean debug) {
		if (entities != null) {
			return "on " + entities.toString(e, debug) + " teleport";
		} else {
			return "on teleport";
		}
	}
}
