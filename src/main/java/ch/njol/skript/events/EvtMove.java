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
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.util.coll.CollectionUtils;
import io.papermc.paper.event.entity.EntityMoveEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;
import org.eclipse.jdt.annotation.Nullable;

public class EvtMove extends SkriptEvent {

	private static final boolean HAS_ENTITY_MOVE = Skript.classExists("io.papermc.paper.event.entity.EntityMoveEvent");

	static {
		Class<? extends Event>[] events;
		if (HAS_ENTITY_MOVE)
			events = CollectionUtils.array(PlayerMoveEvent.class, EntityMoveEvent.class);
		else
			events = CollectionUtils.array(PlayerMoveEvent.class);

		Skript.registerEvent("Move", EvtMove.class, events, "%entitydata% (move|walk|step)")
			.description("Called when a player or entity moves.",
				"NOTE: Move event will only be called when the entity/player moves position, not orientation (ie: looking around).",
				"NOTE: These events can be performance heavy as they are called quite often.",
				"If you use these events, and later remove them, a server restart is recommended to clear registered events from Skript.")
			.examples("on player move:",
				"\tif player does not have permission \"player.can.move\":",
				"\t\tcancel event",
				"on skeleton move:",
				"\tif event-entity is not in world \"world\":",
				"\t\tkill event-entity")
			.requiredPlugins("Paper 1.16.5+ (entity move)")
			.since("2.6");
	}

	private EntityData<?> type;
	private boolean isPlayer;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		type = ((Literal<EntityData<?>>) args[0]).getSingle();
		isPlayer = Player.class.isAssignableFrom(type.getType());

		if (!HAS_ENTITY_MOVE && !isPlayer) {
			Skript.error("Entity move event requires Paper 1.16.5+", ErrorQuality.SEMANTIC_ERROR);
			return false;
		}
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (isPlayer && event instanceof PlayerMoveEvent) {
			PlayerMoveEvent playerEvent = (PlayerMoveEvent) event;
			return moveCheck(playerEvent.getFrom(), playerEvent.getTo());
		} else if (HAS_ENTITY_MOVE && event instanceof EntityMoveEvent) {
			EntityMoveEvent entityEvent = (EntityMoveEvent) event;
			if (type.isInstance(entityEvent.getEntity())) {
				return moveCheck(entityEvent.getFrom(), entityEvent.getTo());
			}
		}
		return false;
	}

	@Override
	@Nullable
	@SuppressWarnings("unchecked")
	public Class<? extends Event> [] getEventClasses() {
		if (isPlayer) {
			return new Class[] {PlayerMoveEvent.class};
		} else if (HAS_ENTITY_MOVE) {
			return new Class[] {EntityMoveEvent.class};
		} else {
			return null;
		}
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return type + " move";
	}

	private static boolean moveCheck(Location from, Location to) {
		return from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ() || from.getWorld() != to.getWorld();
	}

}
