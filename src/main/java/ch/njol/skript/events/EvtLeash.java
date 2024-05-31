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
import ch.njol.skript.registrations.Classes;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.player.PlayerUnleashEntityEvent;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"unchecked", "NotNullFieldNotInitialized"})
public class EvtLeash extends SkriptEvent {

	static {
		Skript.registerEvent("Leash / Unleash", EvtLeash.class, CollectionUtils.array(PlayerLeashEntityEvent.class, EntityUnleashEvent.class), "[:player] [:un]leash[ing] [[of] %-entitydatas%]")
			.description("Called when an entity is leashed or unleashed. Cancelling these events will prevent the leashing or unleashing from occurring.")
			.examples(
					"on player leash of a sheep:",
						"\tsend \"Baaaaa--\" to player",
					"",
					"on player leash:",
						"\tsend \"<%event-entity%> Let me go!\" to player",
					"",
					"on unleash:",
						"\tbroadcast \"<%event-entity%> I'm free\"",
					"",
					"on player unleash:",
						"\tsend \"<%event-entity%> Thanks for free-ing me!\" to player"
			)
			.since("INSERT VERSION");
	}

	private enum EventType {

		LEASH("leash"),
		UNLEASH("unleash"),
		UNLEASH_BY_PLAYER("player unleash");

		private final String name;

		EventType(String name) {
			this.name = name;
		}


		@Override
		public String toString() {
			return name;
		}

	}

	@Nullable
	private EntityData<?>[] types;
	private EventType eventType;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		types = args[0] == null ? null : ((Literal<EntityData<?>>) args[0]).getAll();
		eventType = EventType.LEASH;
		if (parseResult.hasTag("un")) {
			eventType = EventType.UNLEASH;
			if (parseResult.hasTag("player")) {
				eventType = EventType.UNLEASH_BY_PLAYER;
			}
		}
		return true;
	}

	@Override
	public boolean check(Event event) {
		Entity leashedEntity;
		switch (eventType) {
            case LEASH:
				if (!(event instanceof PlayerLeashEntityEvent))
					return false;
				leashedEntity = ((PlayerLeashEntityEvent) event).getEntity();
				break;
            case UNLEASH:
				if (!(event instanceof EntityUnleashEvent))
					return false;
				leashedEntity = ((EntityUnleashEvent) event).getEntity();
                break;
            case UNLEASH_BY_PLAYER:
				if (!(event instanceof PlayerUnleashEntityEvent))
					return false;
				leashedEntity = ((PlayerUnleashEntityEvent) event).getEntity();
                break;
			default:
				return false;
        }
		if (types == null)
			return true;
		for (EntityData<?> entityData : types) {
			if (entityData.isInstance(leashedEntity))
				return true;
		}
		return false;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return eventType + (types != null ? " " + Classes.toString(types, false) : "");
	}

}
