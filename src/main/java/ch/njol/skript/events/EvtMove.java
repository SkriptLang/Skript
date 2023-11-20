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
import ch.njol.util.coll.CollectionUtils;
import io.papermc.paper.event.entity.EntityMoveEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.Nullable;

public class EvtMove extends SkriptEvent {

	private static final boolean HAS_ENTITY_MOVE = Skript.classExists("io.papermc.paper.event.entity.EntityMoveEvent");

	static {
		Class<? extends Event>[] events;
		if (HAS_ENTITY_MOVE)
			events = CollectionUtils.array(PlayerMoveEvent.class, EntityMoveEvent.class);
		else
			events = CollectionUtils.array(PlayerMoveEvent.class);
		Skript.registerEvent("Move / Rotate", EvtMove.class, events,
				"%entitydata% (move|walk|step|rotate:(turn[ing] around|rotate))",
				"%entitydata% (move|walk|step) or (turn[ing] around|rotate)",
				"%entitydata% (turn[ing] around|rotate) or (move|walk|step)")
				.description(
						"Called when a player or entity moves or rotates their head.",
						"NOTE: Move event will only be called when the entity/player moves position, keyword 'turn around' is for orientation (ie: looking around), and the combined syntax listens for both.",
						"NOTE: These events can be performance heavy as they are called quite often.")
				.examples(
						"on player move:",
							"\tif player does not have permission \"player.can.move\":",
								"\t\tcancel event",
						"on skeleton move:",
							"\tif event-entity is not in world \"world\":",
								"\t\tkill event-entity",
						"on player turning around:",
							"send action bar \"You are currently turning your head around!\" to player")
				.requiredPlugins("Paper 1.16.5+ (entity move)")
				.since("2.6, INSERT VERSION (turn around)");
	}

	private EntityData<?> entityData;
	private boolean isPlayer;
	private Move moveType;

	private enum Move {

		MOVE("move"),
		MOVE_OR_ROTATE("move or rotate"),
		ROTATE("rotate");

		private final String name;

		Move(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}

	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		entityData = ((Literal<EntityData<?>>) args[0]).getSingle();
		isPlayer = Player.class.isAssignableFrom(entityData.getType());
		if (!HAS_ENTITY_MOVE && !isPlayer) {
			Skript.error("Entity move event requires Paper 1.16.5+");
			return false;
		}
		if (matchedPattern > 0) {
			moveType = Move.MOVE_OR_ROTATE;
		} else if (parseResult.hasTag("rotate")) {
			moveType = Move.ROTATE;
		} else {
			moveType = Move.MOVE;
		}
		return true;
	}

	@Override
	public boolean check(Event event) {
		Location from, to;
		if (isPlayer && event instanceof PlayerMoveEvent) {
			PlayerMoveEvent playerEvent = (PlayerMoveEvent) event;
			from = playerEvent.getFrom();
			to = playerEvent.getTo();
		} else if (HAS_ENTITY_MOVE && event instanceof EntityMoveEvent) {
			EntityMoveEvent entityEvent = (EntityMoveEvent) event;
			if (!(entityData.isInstance(entityEvent.getEntity())))
				return false;
			from = entityEvent.getFrom();
			to = entityEvent.getTo();
		} else {
			return false;
		}
		switch (moveType) {
			case MOVE:
				return hasChangedPosition(from, to);
			case ROTATE:
				return hasChangedOrientation(from, to);
			case MOVE_OR_ROTATE:
				return true;
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
		}
		return null;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return entityData + " " + moveType;
	}

	private static boolean hasChangedPosition(Location from, Location to) {
		return from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ() || from.getWorld() != to.getWorld();
	}

	private static boolean hasChangedOrientation(Location from, Location to) {
		return from.getYaw() != to.getYaw() || from.getPitch() != to.getPitch();
	}

}
