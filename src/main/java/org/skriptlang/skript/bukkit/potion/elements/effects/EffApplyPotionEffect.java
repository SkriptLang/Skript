package org.skriptlang.skript.bukkit.potion.elements.effects;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.potion.util.PotionUtils;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffect;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Potion Effects")
@Description("Apply potion effects to/from entities.")
@Examples({
	"apply swiftness 2 to the player",
	"on join:",
		"\tapply potion of strength of tier {strength::%player's uuid%} to the player for 999 days",
	"apply potion effects of player's tool to player"
})
@Since("2.0, 2.2-dev27 (ambient and particle-less potion effects), 2.5 (replacing existing effect), 2.5.2 (potion effects), INSERT VERSION (syntax changes)")
public class EffApplyPotionEffect extends Effect {

	public static void register(SyntaxRegistry registry) {
		// While allowing the user to specify the timespan here is repetitive as you can do it in ExprPotionEffect,
		// it allows syntax like "apply haste 3 to the player for 5 seconds" to work
		registry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffApplyPotionEffect.class)
				.addPatterns(
						"apply %potioneffects% to %livingentities% [for %-timespan%]",
						"effect %livingentities% with %potioneffects% [for %-timespan%]"
				)
				.build()
		);
	}

	private Expression<SkriptPotionEffect> potionEffects;
	private Expression<LivingEntity> entities;
	private @Nullable Expression<Timespan> duration;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		boolean first = matchedPattern == 0;
		potionEffects = (Expression<SkriptPotionEffect>) exprs[first ? 0 : 1];
		entities = (Expression<LivingEntity>) exprs[first ? 1 : 0];
		duration = (Expression<Timespan>) exprs[2];
		return true;
	}

	@Override
	protected void execute(Event event) {
		SkriptPotionEffect[] potionEffects = this.potionEffects.getArray(event);

		if (duration != null) {
			Timespan timespan = duration.getSingle(event);
			if (timespan != null) {
				int ticks = (int) timespan.getAs(TimePeriod.TICK);
				for (SkriptPotionEffect potionEffect : potionEffects)
					// TODO not sure if this should actually modify the potion effect
					potionEffect.duration(ticks);
			}
		}

		for (LivingEntity livingEntity : entities.getArray(event)) {
			for (PotionEffect potionEffect : PotionUtils.convertSkriptPotionEffects(potionEffects))
				livingEntity.addPotionEffect(potionEffect);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "apply " + potionEffects.toString(event, debug) + " to " + entities.toString(event, debug);
	}

}
