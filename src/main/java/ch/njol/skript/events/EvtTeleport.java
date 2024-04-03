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
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import javax.annotation.Nullable;

public class EvtTeleport extends SkriptEvent {

	static {
		Skript.registerEvent("Teleport", EvtTeleport.class, CollectionUtils.array(EntityTeleportEvent.class, PlayerTeleportEvent.class), "entity teleport[ing] [of %entitytypes%]", "[player] teleport[ing]")
			.description("This event can be used to teleport non-player or player entities respectively", "When teleporting entities, the event may also be called due to a result of natural causes, such as an Enderman or Shulker teleporting, or Wolfs teleporting to players.", "When teleporting players, the event can be called by teleporting through a nether/end portal, or by other means (e.g. plugins).")
			.examples(
				"on player teleport:",
				"on entity teleport:",
				"on entity teleport of creeper:"
			)
			.since("1.0, INSERT VERSION (entity teleport)");
	}

	@SuppressWarnings("unchecked")
	private Expression<EntityType> entities;

	private int matchedPattern;
	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, SkriptParser.ParseResult parseResult) {
		this.matchedPattern = matchedPattern;
		if (matchedPattern == 0 && args[0] != null) {
			entities = (Expression<EntityType>) args[0];
		}
		return true;
	}


	@Override
	public boolean check(Event event) {
		Entity entity;
		if (event instanceof EntityTeleportEvent) {
			EntityTeleportEvent entityEvent = (EntityTeleportEvent) event;
			entity = entityEvent.getEntity();
			if (entity instanceof Player) {
				return false;
			}
		} else if (event instanceof PlayerTeleportEvent) {
			PlayerTeleportEvent playerEvent = (PlayerTeleportEvent) event;
			entity = playerEvent.getPlayer();
			if (matchedPattern == 0) {
				return false; // Exclude players from "entity teleport event"
			}
		} else {
			return false;
		}
		if (entities != null) {
			for (EntityType entType : entities.getArray(event)) {
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
			return "on entity teleport of " + entities.toString(e, debug);
		} else if (e instanceof PlayerTeleportEvent) {
			return "on player teleport";
		} else {
			return "on entity teleport";
		}
	}
}
