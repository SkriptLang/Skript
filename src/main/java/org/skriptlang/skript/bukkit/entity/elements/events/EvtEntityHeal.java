package org.skriptlang.skript.bukkit.entity.elements.events;

import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.Arrays;

public class EvtEntityHeal extends SkriptEvent {

	public static void register(SyntaxRegistry syntaxRegistry, EventValueRegistry eventValueRegistry) {
		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(EvtEntityHeal.class, "Entity Heal")
			.supplier(EvtEntityHeal::new)
			.addEvent(EntityRegainHealthEvent.class)
			.addPatterns(
				"heal[ing] [entity:of %-entitydatas%] [reason:(from|due to|by) %-healreasons%]",
				"[entity:%-entitydatas%] heal[ing] [reason:(from|due to|by) %-healreasons%]"
			)
			.addDescription("""
				Called when an entity is healed,\s
				e.g. by eating (players),\s
				being fed (pets),\s
				or by the effect of a potion of healing (overworld mobs) or harm (nether mobs).
				""")
			.addExample("""
				on healing of a zombie, cow or a wither:
				    event-heal reason is healing potion
				    cancel event
				""")
			.addExample("""
				on player healing from a regeneration potion:
				    send "all better!" to event-entity
				""")
			.addSince("1.0, 2.9.0 (by reason)")
			.build());

		eventValueRegistry.register(EventValue.builder(EntityRegainHealthEvent.class, RegainReason.class)
			.getter(EntityRegainHealthEvent::getRegainReason)
			.build());
	}

	private EntityData<?>[] entityData;
	private RegainReason[] reasons;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		if (parseResult.hasTag("reason")) {
			Literal<RegainReason> reasonLiteral = (Literal<RegainReason>) args[1];
			reasons = reasonLiteral.getArray();
		}
		if (parseResult.hasTag("entity")) {
			Literal<EntityData<?>> entityLiteral = (Literal<EntityData<?>>) args[0];
			entityData = entityLiteral.getArray();
		}
		return true;
	}

	@Override
	public boolean check(Event event) {
		EntityRegainHealthEvent entityEvent = (EntityRegainHealthEvent) event;
		boolean entityMatched = true;
		boolean reasonMatched = true;
		if (entityData != null)
		     entityMatched = Arrays.stream(entityData).noneMatch(data -> data.isInstance(entityEvent.getEntity()));
		if (reasons != null)
			reasonMatched = Arrays.stream(reasons).noneMatch(reason -> reason == entityEvent.getRegainReason());
		return entityMatched && reasonMatched;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append(entityData != null ? entityData : "entity")
			.append("healing")
			.appendIf(reasons != null, "due to", reasons)
			.toString();
	}

}
