package ch.njol.skript.events;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.player.PlayerUnleashEntityEvent;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.EventValues;
import ch.njol.util.coll.CollectionUtils;

public class EvtLeash extends SkriptEvent {

	static {
		Skript.registerEvent("Leash / Unleash", EvtLeash.class, CollectionUtils.array(PlayerLeashEntityEvent.class, EntityUnleashEvent.class), "[:player] [:un]leash[ing] [of %-entitydatas%]")
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
						"\tsend \"<%event-entity%> Thanks for freeing me!\" to player"
			)
			.since("2.10");

		// PlayerLeashEntityEvent
		// event-player is explicitly registered due to event does not extend PlayerEvent
		EventValues.registerEventValue(PlayerLeashEntityEvent.class, Player.class, PlayerLeashEntityEvent::getPlayer);
		EventValues.registerEventValue(PlayerLeashEntityEvent.class, Entity.class, PlayerLeashEntityEvent::getEntity);

		// EntityUnleashEvent
		EventValues.registerEventValue(EntityUnleashEvent.class, EntityUnleashEvent.UnleashReason.class, EntityUnleashEvent::getReason);

		// PlayerUnleashEntityEvent
		EventValues.registerEventValue(PlayerUnleashEntityEvent.class, Player.class, PlayerUnleashEntityEvent::getPlayer);
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

	private @Nullable EntityData<?>[] types;
	private EventType eventType;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		//noinspection unchecked
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
            case LEASH -> {
				if (!(event instanceof PlayerLeashEntityEvent playerLeash))
					return false;
				leashedEntity = playerLeash.getEntity();
			}
            case UNLEASH -> {
				if (!(event instanceof EntityUnleashEvent entityUnleash))
					return false;
				leashedEntity = entityUnleash.getEntity();
			}
            case UNLEASH_BY_PLAYER -> {
				if (!(event instanceof PlayerUnleashEntityEvent playerUnleash))
					return false;
				leashedEntity = playerUnleash.getEntity();
			}
            default -> {
                return false;
            }
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
		return eventType + (types != null ? " of " + Classes.toString(types, false) : "");
	}

}
