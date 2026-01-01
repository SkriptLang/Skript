package org.skriptlang.skript.bukkit.potion.elements.events;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.registrations.EventValues;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.docs.Origin;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.Arrays;

public class EvtEntityPotion extends SkriptEvent {

	public static void register(SyntaxRegistry registry, Origin origin) {
		registry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(EvtEntityPotion.class, "Entity Potion Effect")
				.supplier(EvtEntityPotion::new)
				.origin(origin)
				.addEvent(EntityPotionEffectEvent.class)
				.addPattern("entity potion effect [modif[y|ication]] [[of] %-potioneffecttypes%] [%-potionactions%] [due to %-potioncauses%]")
				.addDescription("Called when an entity's potion effect is modified.")
				.addExamples(
					"on entity potion effect modification:",
						"\tbroadcast \"A potion effect was added to %event-entity%!\"",
					"",
					"on entity potion effect of night vision added:",
						"\tmessage \"You can now see in the dark!\"",
					"",
					"on entity potion effect of strength removed:",
						"\tmessage \"You're now weaker!\""
				)
				.addSince("2.10", "INSERT VERSION (action support)")
				.build());

		// Entity Potion Effect
		EventValues.registerEventValue(EntityPotionEffectEvent.class, PotionEffect.class, EntityPotionEffectEvent::getOldEffect, EventValues.TIME_PAST);
		EventValues.registerEventValue(EntityPotionEffectEvent.class, PotionEffect.class, EntityPotionEffectEvent::getNewEffect);
		EventValues.registerEventValue(EntityPotionEffectEvent.class, PotionEffectType.class, EntityPotionEffectEvent::getModifiedType);
		EventValues.registerEventValue(EntityPotionEffectEvent.class, EntityPotionEffectEvent.Cause.class, EntityPotionEffectEvent::getCause);
		EventValues.registerEventValue(EntityPotionEffectEvent.class, EntityPotionEffectEvent.Action.class, EntityPotionEffectEvent::getAction);
	}

	private Literal<EntityPotionEffectEvent.Action> actions;
	private Expression<PotionEffectType> types;
	private Literal<EntityPotionEffectEvent.Cause> causes;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		types = (Expression<PotionEffectType>) args[0];
		actions = (Literal<EntityPotionEffectEvent.Action>) args[1];
		causes = (Literal<EntityPotionEffectEvent.Cause>) args[2];
		return true;
	}

	@Override
	public boolean check(Event event) {
		EntityPotionEffectEvent potionEvent = (EntityPotionEffectEvent) event;

		if (actions != null && Arrays.stream(actions.getAll()).noneMatch(action -> action == potionEvent.getAction())) {
			return false;
		}

		if (types != null) {
			PotionEffectType oldType = potionEvent.getOldEffect() != null ? potionEvent.getOldEffect().getType() : null;
			PotionEffectType newType = potionEvent.getNewEffect() != null ? potionEvent.getNewEffect().getType() : null;
			if (!types.check(event, type -> type.equals(oldType) || type.equals(newType))) {
				return false;
			}
		}

		return causes == null || Arrays.stream(causes.getAll()).anyMatch(cause -> cause == potionEvent.getCause());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append("on entity potion effect modification");
		if (types != null) {
			builder.append("of", types);
		}
		if (actions != null) {
			builder.append(actions);
		}
		if (causes != null) {
			builder.append("due to", causes);
		}
		return builder.toString();
	}

}
