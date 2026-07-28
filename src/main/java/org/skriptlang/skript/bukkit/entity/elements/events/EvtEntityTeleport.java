package org.skriptlang.skript.bukkit.entity.elements.events;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.LiteralList;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Location;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import ch.njol.skript.entity.EntityData;
import org.jetbrains.annotations.Nullable;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import org.bukkit.event.Event;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue.Time;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EvtEntityTeleport extends SkriptEvent {

	public static void register(SyntaxRegistry syntaxRegistry, EventValueRegistry eventValueRegistry) {
		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(EvtEntityTeleport.class, "Entity Teleport")
			.supplier(EvtEntityTeleport::new)
			.addEvents(CollectionUtils.array(EntityTeleportEvent.class, PlayerTeleportEvent.class))
			.addPatterns("[%-entitydatas%] teleport[ing]")
			.addDescription("""
				Called when an entity teleports including players.
				This event will also be called due to a result of natural causes,\s
				such as an enderman or shulker teleporting,
				or wolves teleporting to players.\s
				When teleporting players, the event can be called by teleporting through a nether/end portal,\s
				or by other means (e.g. plugins).
				""")
			.addExample("""
				on player teleport:
					send "Well you somehow teleported.." to player
				""")
			.addExample("""
				on wolf teleport:
					send "Your wolf just teleported!" to owner of event-entity
				""")
			.addSince("1.0")
			.addSince("2.9.0 (entity teleport)")
			.build());

		eventValueRegistry.register(EventValue.builder(PlayerTeleportEvent.class, Location.class)
			.getter(PlayerTeleportEvent::getFrom)
			.registerChanger(ChangeMode.SET, PlayerTeleportEvent::setFrom)
			.time(Time.PAST)
			.build());

		eventValueRegistry.register(EventValue.builder(PlayerTeleportEvent.class, Location.class)
			.getter(PlayerTeleportEvent::getTo)
			.registerChanger(ChangeMode.SET, PlayerTeleportEvent::setTo)
			.build());

		eventValueRegistry.register(EventValue.builder(EntityTeleportEvent.class, Location.class)
			.getter(EntityTeleportEvent::getFrom)
			.registerChanger(ChangeMode.SET, EntityTeleportEvent::setFrom)
			.time(Time.PAST)
			.build());

		eventValueRegistry.register(EventValue.builder(EntityTeleportEvent.class, Location.class)
			.getter(EntityTeleportEvent::getTo)
			.registerChanger(ChangeMode.SET, EntityTeleportEvent::setTo)
			.build());
	}

	private @Nullable Literal<EntityData<?>> entityData;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		if (args[0] != null) {
			entityData = (Literal<EntityData<?>>) args[0];
			if (entityData.getAnd() && entityData instanceof LiteralList<EntityData<?>> list)
				list.invertAnd();
		}
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (entityData == null)
			return true;
		if (event instanceof EntityTeleportEvent entityEvent) {
			return entityData.check(event, data -> data.isInstance(entityEvent.getEntity()));
		} else if (event instanceof PlayerTeleportEvent playerEvent) {
			return entityData.check(event, data -> data.isInstance(playerEvent.getPlayer()));
		}
		return false;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.appendIf(entityData != null, entityData)
			.append("teleporting")
			.toString();
	}

}

