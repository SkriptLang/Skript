package org.skriptlang.skript.bukkit.entity.elements.events;

import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.LiteralList;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.bukkit.event.entity.EntityUnleashEvent.UnleashReason;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.player.PlayerUnleashEntityEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EvtEntityLeash extends SkriptEvent {

	public static void register(SyntaxRegistry syntaxRegistry, EventValueRegistry eventValueRegistry) {
		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(EvtEntityLeash.class, "Entity Leash")
			.supplier(EvtEntityLeash::new)
			.addEvents(CollectionUtils.array(PlayerLeashEntityEvent.class, EntityUnleashEvent.class))
			.addPatterns("[:player] [:un]leash[ing] [of %-entitydatas%]")
			.addDescription("""
				Called when an entity is leashed or unleashed.
				Cancelling these events will prevent the leashing or unleashing from occurring.
				""")
			.addExample("""
				on player leash of a sheep:
					send "Baaaaa--" to player
				""")
			.addExample("""
				on player leash:
					send "<%event-entity%> Let me go!" to player
				""")
			.addExample("""
				on unleash:
					broadcast "<%event-entity%> I'm free!"
				""")
			.addSince("2.10")
			.build());

		eventValueRegistry.register(EventValue.builder(PlayerLeashEntityEvent.class, Player.class)
			.getter(PlayerLeashEntityEvent::getPlayer)
			.build());

		eventValueRegistry.register(EventValue.builder(PlayerLeashEntityEvent.class, Entity.class)
			.getter(PlayerLeashEntityEvent::getEntity)
			.build());

		eventValueRegistry.register(EventValue.builder(EntityUnleashEvent.class, UnleashReason.class)
			.getter(EntityUnleashEvent::getReason)
			.build());

		eventValueRegistry.register(EventValue.builder(PlayerUnleashEntityEvent.class, Player.class)
			.getter(PlayerUnleashEntityEvent::getPlayer)
			.build());

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

	private @Nullable Literal<EntityData<?>> entityData;
	private EventType eventType;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		if (args[0] != null) {
			entityData = (Literal<EntityData<?>>) args[0];
			if (entityData.getAnd() && entityData instanceof LiteralList<EntityData<?>> list)
				list.invertAnd();
		}

		eventType = EventType.LEASH;
		if (parseResult.hasTag("un")) {
			eventType = EventType.UNLEASH;
			if (parseResult.hasTag("player"))
				eventType = EventType.UNLEASH_BY_PLAYER;
		}
		return true;
	}

	@Override
	public boolean check(Event event) {
		Entity leashedEntity = switch (eventType) {
			case LEASH -> event instanceof PlayerLeashEntityEvent playerLeash ? playerLeash.getEntity() : null;
			case UNLEASH -> event instanceof EntityUnleashEvent entityUnleash ? entityUnleash.getEntity() : null;
			case UNLEASH_BY_PLAYER -> event instanceof PlayerUnleashEntityEvent playerUnleash ? playerUnleash.getEntity() : null;
		};

		if (leashedEntity == null)
			return false;
		if (entityData == null)
			return true;
		return entityData.check(event, data -> data.isInstance(leashedEntity));

	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append(eventType)
			.appendIf(entityData != null, "of", entityData)
			.toString();
	}

}
