package org.skriptlang.skript.bukkit.potion.elements;

import ch.njol.skript.Skript;
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
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;
import org.skriptlang.skript.bukkit.registration.BukkitRegistryKeys;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

import javax.annotation.Nullable;

import static ch.njol.skript.registrations.EventValues.TIME_PAST;

public class EvtEntityPotion extends SkriptEvent {

	public static void register(SyntaxRegistry registry) {
		registry.register(BukkitRegistryKeys.EVENT, BukkitSyntaxInfos.Event.builder(EvtEntityPotion.class, "Entity Potion Effect")
				.addEvent(EntityPotionEffectEvent.class)
				.addPattern("entity potion effect [modif[y|ication]] [[of] %-potioneffecttypes%] [due to %-entitypotioncause%]")
				.addDescription("Called when an entity's potion effect is modified.", "This modification can include adding, removing or changing their potion effect.")
				.addExamples(
					"on entity potion effect modification:",
						"\tbroadcast \"A potion effect was added to %event-entity%!\" ",
					"",
					"on entity potion effect modification of night vision:"
				)
				.since("2.10")
				.build());

		// Entity Potion Effect
		EventValues.registerEventValue(EntityPotionEffectEvent.class, SkriptPotionEffect.class, event -> {
			PotionEffect effect = event.getOldEffect();
			if (effect == null) {
				return null;
			}
			return new SkriptPotionEffect(effect);
		}, TIME_PAST);
		EventValues.registerEventValue(EntityPotionEffectEvent.class, SkriptPotionEffect.class, event -> {
			PotionEffect effect = event.getNewEffect();
			if (effect == null) {
				return null;
			}
			return new SkriptPotionEffect(effect);
		});
		EventValues.registerEventValue(EntityPotionEffectEvent.class, PotionEffectType.class, EntityPotionEffectEvent::getModifiedType);
		EventValues.registerEventValue(EntityPotionEffectEvent.class, EntityPotionEffectEvent.Cause.class, EntityPotionEffectEvent::getCause);
	}

	private Expression<PotionEffectType> potionEffects;
	private Expression<EntityPotionEffectEvent.Cause> cause;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		potionEffects = (Expression<PotionEffectType>) args[0];
		cause = (Expression<EntityPotionEffectEvent.Cause>) args[1];
		return true;
	}

	@Override
	public boolean check(Event event) {
		EntityPotionEffectEvent potionEvent = (EntityPotionEffectEvent) event;

		if (potionEffects != null) {
			PotionEffectType oldType = potionEvent.getOldEffect() != null ? potionEvent.getOldEffect().getType() : null;
			PotionEffectType newType = potionEvent.getNewEffect() != null ? potionEvent.getNewEffect().getType() : null;
			if (!potionEffects.check(event, type -> type.equals(oldType) || type.equals(newType))) {
				return false;
			}
		}

		return cause == null || cause.check(event, cause -> cause.equals(potionEvent.getCause()));
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append("on entity potion effect modification");
		if (potionEffects != null) {
			builder.append("of", potionEffects);
		}
		if (cause != null) {
			builder.append("due to", cause);
		}
		return builder.toString();
	}

}
