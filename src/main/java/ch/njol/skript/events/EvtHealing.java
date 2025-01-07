package ch.njol.skript.events;

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;

public class EvtHealing extends SkriptEvent {

	static {
		Skript.registerEvent("Heal", EvtHealing.class, EntityRegainHealthEvent.class, "heal[ing] [of %-entitydatas%] [(from|due to|by) %-healreasons%]", "%entitydatas% heal[ing] [(from|due to|by) %-healreasons%]")
				.description("Called when an entity is healed, e.g. by eating (players), being fed (pets), or by the effect of a potion of healing (overworld mobs) or harm (nether mobs).")
				.examples(
						"on heal:",
						"on player healing from a regeneration potion:",
						"on healing of a zombie, cow or a wither:",
								"\theal reason is healing potion",
								"\tcancel event"
				)
				.since("1.0, 2.9.0 (by reason)");
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
		if (!(event instanceof EntityRegainHealthEvent))
			return false;
		EntityRegainHealthEvent healthEvent = (EntityRegainHealthEvent) event;
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
			if (!result)
				return false;
		}
		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "heal" + (entityDatas != null ? " of " + entityDatas.toString(event, debug) : "") +
				(healReasons != null ? " by " + healReasons.toString(event, debug) : "");
	}

}
