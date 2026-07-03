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

public class EvtEntityHeal extends SkriptEvent {

	public static void register(SyntaxRegistry syntaxRegistry, EventValueRegistry eventValueRegistry) {
		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(EvtEntityHeal.class, "Entity Heal")
			.supplier(EvtEntityHeal::new)
			.addEvent(EntityRegainHealthEvent.class)
			.addPatterns(
				"heal[ing] [of %-entitydatas%] [(from|due to|by) %-healreasons%]",
				"[%-entitydatas%] heal[ing] [(from|due to|by) %-healreasons%]"
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

	private Literal<EntityData<?>> entityData;
	private Literal<RegainReason> reasons;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		if (args[0] != null)
			entityData = (Literal<EntityData<?>>) args[0];

		if (args[1] != null)
			reasons = (Literal<RegainReason>) args[1];
		return true;
	}

	@Override
	public boolean check(Event event) {
		EntityRegainHealthEvent entityEvent = (EntityRegainHealthEvent) event;
		if (entityData != null && !entityData.check(event, data -> data.isInstance(entityEvent.getEntity())))
			return false;
		if (reasons != null && !reasons.check(event, reason -> reason.equals(entityEvent.getRegainReason())))
			return false;
		return true;

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
