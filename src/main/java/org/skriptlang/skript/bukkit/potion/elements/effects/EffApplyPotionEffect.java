package org.skriptlang.skript.bukkit.potion.elements.effects;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffect;
import org.skriptlang.skript.docs.Origin;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Apply Potion Effect")
@Description("Applies a potion effect to an entity.")
@Example("apply swiftness 2 to the player")
@Example("""
	command /strengthboost:
		trigger:
			apply strength 10 to the player for 5 minutes
	""")
@Example("apply the potion effects of the player's tool to the player")
@Since({"2.0", "2.14 (syntax rework)"})
public class EffApplyPotionEffect extends Effect {

	public static void register(SyntaxRegistry registry, Origin origin) {
		// While allowing the user to specify the timespan here is repetitive as you can do it in ExprSecPotionEffect,
		// it allows syntax like "apply haste 3 to the player for 5 seconds" to work
		registry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffApplyPotionEffect.class)
				.supplier(EffApplyPotionEffect::new)
				.origin(origin)
				.addPatterns(
						"(apply|grant) %skriptpotioneffects% to %livingentities% [for %-timespan%]",
						"(affect|afflict) %livingentities% with %skriptpotioneffects% [for %-timespan%]"
				)
				.build());
	}

	private Expression<SkriptPotionEffect> potions;
	private Expression<LivingEntity> entities;
	private @Nullable Expression<Timespan> duration;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		boolean first = matchedPattern == 0;
		potions = (Expression<SkriptPotionEffect>) exprs[first ? 0 : 1];
		entities = (Expression<LivingEntity>) exprs[first ? 1 : 0];
		duration = (Expression<Timespan>) exprs[2];
		return true;
	}

	@Override
	protected void execute(Event event) {
		SkriptPotionEffect[] potions = this.potions.getArray(event);

		if (duration != null) {
			Timespan timespan = duration.getSingle(event);
			if (timespan != null) {
				if (timespan.isInfinite()) {
					for (int i = 0; i < potions.length; i++) {
						potions[i] = potions[i].clone().infinite(true);
					}
				} else {
					int ticks = (int) timespan.getAs(TimePeriod.TICK);
					for (int i = 0; i < potions.length; i++) {
						potions[i] = potions[i].clone().duration(ticks);
					}
				}
			}
		}

		for (SkriptPotionEffect skriptPotionEffect : potions) {
			PotionEffect potionEffect = skriptPotionEffect.asBukkitPotionEffect();
			for (LivingEntity livingEntity : entities.getArray(event)) {
				livingEntity.addPotionEffect(potionEffect);
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "apply " + potions.toString(event, debug) + " to " + entities.toString(event, debug);
	}

}
