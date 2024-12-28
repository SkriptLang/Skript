package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.potion.PotionEffectType;

import javax.annotation.Nullable;

public class EvtEntityPotion extends SkriptEvent {

	static {
		Skript.registerEvent("Entity Potion Effect", EvtEntityPotion.class, EntityPotionEffectEvent.class,
				"entity potion effect [modif[y|ication]] [[of] %-potioneffecttypes%] [due to %-entitypotioncause%]")
			.description("Called when an entity's potion effect is modified.", "This modification can include adding, removing or changing their potion effect.")
			.examples(
				"on entity potion effect modification:",
					"\t\tbroadcast \"A potion effect was added to %event-entity%!\" ",
				"",
				"on entity potion effect modification of night vision:")
			.since("INSERT VERSION");
	}

	@SuppressWarnings("unchecked")
	private Expression<PotionEffectType> potionEffects;
	private Expression<EntityPotionEffectEvent.Cause> cause;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		potionEffects = (Expression<PotionEffectType>) args[0];
		cause = (Expression<EntityPotionEffectEvent.Cause>) args[1];
		return true;
	}

	@Override
	public boolean check(Event event) {
		EntityPotionEffectEvent potionEvent = (EntityPotionEffectEvent) event;
		boolean effectMatches = potionEffects == null ||
			(potionEvent.getOldEffect() != null && potionEffects.check(event, effectType -> effectType.equals(potionEvent.getOldEffect().getType()))) ||
			(potionEvent.getNewEffect() != null && potionEffects.check(event, effectType -> effectType.equals(potionEvent.getNewEffect().getType())));

		boolean causeMatches = cause == null || cause.check(event, cause -> cause.equals(potionEvent.getCause()));

		return effectMatches && causeMatches;
	}


	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "on entity potion effect modification";
	}
}
