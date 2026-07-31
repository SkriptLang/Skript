package org.skriptlang.skript.bukkit.entity.player.elements.events;

import ch.njol.skript.lang.SyntaxStringBuilder;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EvtHealing extends SkriptEvent {

	public static void register(SyntaxRegistry syntaxRegistry, EventValueRegistry registry) {
		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(EvtHealing.class, "Healing")
			.supplier(EvtHealing::new)
			.addEvent(EntityRegainHealthEvent.class)
			.addPattern("heal[ing] [of %-entitydatas%] [(from|due to|by) %-healreasons%]")
			.addPattern("%entitydatas% heal[ing] [(from|due to|by) %-healreasons%]")
			.addDescription("""
				Called when an entity is healed, e.g. by eating (players), being fed (pets), or by the effect of a potion of healing (overworld mobs) or harm (nether mobs).
				""")
			.addExample("on heal")
			.addExample("on player healing from a regeneration potion:")
			.addExample("""
				on healing of a zombie, cow or a wither:
					heal reason is healing potion
					cancel event
				""")
			.addSince("1.0")
			.addSince("2.9.0 (by reason)")
			.build());

		registry.register(EventValue.builder(EntityRegainHealthEvent.class, RegainReason.class)
			.getter(EntityRegainHealthEvent::getRegainReason)
			.patterns("healreason")
			.build());
	}

	@Nullable
	private Literal<EntityData<?>> entityDatas;

	@Nullable
	private Literal<RegainReason> healReasons;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parser) {
		entityDatas = (Literal<EntityData<?>>) args[0];
		healReasons = (Literal<RegainReason>) args[1];
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (!(event instanceof EntityRegainHealthEvent healthEvent))
			return false;

		if (entityDatas != null) {
			Entity compare = healthEvent.getEntity();
			boolean result = false;

			for (EntityData<?> entityData : entityDatas.getAll()) {
				if (entityData.isInstance(compare)) {
					result = true;
					break;
				}
			}

			if (!result)
				return false;
		}

		if (healReasons != null) {
			RegainReason compare = healthEvent.getRegainReason();
			boolean result = false;

			for (RegainReason healReason : healReasons.getAll()) {
				if (healReason == compare) {
					result = true;
					break;
				}
			}

			return result;
		}

		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);

		builder.append("heal");

		if (entityDatas != null) {
			builder.append("of");
			builder.append(entityDatas);
		}

		if (healReasons != null) {
			builder.append("by");
			builder.append(healReasons);
		}

		return builder.toString();
	}

}
