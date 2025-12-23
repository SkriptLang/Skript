package org.skriptlang.skript.bukkit.potion.elements.effects;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import ch.njol.util.Kleenean;
import ch.njol.util.Math2;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.potion.util.PotionUtils;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.skriptlang.skript.docs.Origin;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Poison/Cure")
@Description("Poison or cure an entity. If the entity is already poisoned, the duration may be overwritten.")
@Example("poison the player")
@Example("poison the victim for 20 seconds")
@Example("cure the player from of poison")
@Since("1.3.2")
public class EffPoison extends Effect {

	public static void register(SyntaxRegistry syntaxRegistry, Origin origin) {
		syntaxRegistry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffPoison.class)
				.supplier(EffPoison::new)
				.origin(origin)
				.addPatterns(
						"poison %livingentities% [for %-timespan%]",
						"(cure|unpoison) %livingentities% [(from|of) poison]"
				)
				.build());
	}

	private Expression<LivingEntity> entities;
	private @Nullable Expression<Timespan> duration;

	private boolean cure;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		entities = (Expression<LivingEntity>) exprs[0];
		if (matchedPattern == 0) {
			duration = (Expression<Timespan>) exprs[1];
		}
		cure = matchedPattern == 1;
		return true;
	}
	
	@Override
	protected void execute(Event event) {
		if (cure) {
			for (LivingEntity entity : entities.getArray(event)) {
				entity.removePotionEffect(PotionEffectType.POISON);
			}
		} else {
			int duration = PotionUtils.DEFAULT_DURATION_TICKS;
			if (this.duration != null) {
				Timespan timespan = this.duration.getSingle(event);
				if (timespan != null) {
					duration = (int) Math2.fit(0, timespan.getAs(TimePeriod.TICK), Integer.MAX_VALUE);
				}
			}
			for (LivingEntity livingEntity : entities.getArray(event)) {
				int specificDuration = duration;
				if (livingEntity.hasPotionEffect(PotionEffectType.POISON)) { // if the entity is already poisoned, increase the duration
					//noinspection ConstantConditions - PotionEffect cannot be null (checked above)
					int existingDuration = livingEntity.getPotionEffect(PotionEffectType.POISON).getDuration();
					specificDuration = Math2.fit(0, specificDuration + existingDuration, Integer.MAX_VALUE);
				}
				livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.POISON, specificDuration, 0));
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		if (cure) {
			builder.append("cure");
		} else {
			builder.append("poison");
		}
		builder.append(entities);
		if (duration != null) {
			builder.append("for", duration);
		}
		return builder.toString();
	}
	
}
