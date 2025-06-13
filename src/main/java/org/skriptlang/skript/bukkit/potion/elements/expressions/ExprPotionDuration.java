package org.skriptlang.skript.bukkit.potion.elements.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import ch.njol.util.Kleenean;
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.potion.util.PotionUtils;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.ArrayList;
import java.util.List;

@Name("Potion Effect - Duration")
@Description("An expression to obtain the duration of a potion effect.")
@Example("set the duration of {_potion} to 10 seconds")
@Example("add 10 seconds to the duration of speed for all players")
@Since("INSERT VERSION")
public class ExprPotionDuration extends SimpleExpression<Timespan> {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprPotionDuration.class, Timespan.class)
				.supplier(ExprPotionDuration::new)
				.priority(PropertyExpression.DEFAULT_PRIORITY)
				.addPatterns(
					"[the] [potion] (duration|length)[s] of %skriptpotioneffects%",
					"%skriptpotioneffects%'[s] [potion] (duration|length)[s]",
					"[the] [potion] (duration|length)[s] of %potioneffecttypes% (of|for|on) %livingentities%"
				)
				.build());
	}

	private @Nullable Expression<SkriptPotionEffect> potions;
	private @Nullable Expression<PotionEffectType> types;
	private @Nullable Expression<LivingEntity> entities;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (matchedPattern == 2) {
			types = (Expression<PotionEffectType>) expressions[0];
			entities = (Expression<LivingEntity>) expressions[1];
		} else {
			potions = (Expression<SkriptPotionEffect>) expressions[0];
		}
		return true;
	}

	@Override
	protected Timespan @Nullable [] get(Event event) {
		List<Timespan> timespans = new ArrayList<>();
		if (potions != null) {
			for (SkriptPotionEffect potionEffect : potions.getArray(event)) {
				timespans.add(new Timespan(TimePeriod.TICK, potionEffect.infinite() ? Long.MAX_VALUE : potionEffect.duration()));
			}
		} else {
			//noinspection DataFlowIssue - not null by potions==null
			PotionEffectType[] types = this.types.getArray(event);
			//noinspection DataFlowIssue - not null by potions==null
			for (LivingEntity entity : entities.getArray(event)) {
				for (PotionEffectType type : types) {
					PotionEffect potionEffect = entity.getPotionEffect(type);
					if (potionEffect == null) {
						continue;
					}
					int duration = potionEffect.getDuration();
					if (duration == PotionEffect.INFINITE_DURATION) {
						duration = Integer.MAX_VALUE;
					}
					timespans.add(new Timespan(TimePeriod.TICK, duration));
				}
			}
		}
		return timespans.toArray(new Timespan[0]);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case ADD, SET, REMOVE, RESET -> CollectionUtils.array(Timespan.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Timespan change = delta != null ? (Timespan) delta[0] : new Timespan(TimePeriod.TICK, PotionUtils.DEFAULT_DURATION_TICKS);

		if (potions != null) {
			for (SkriptPotionEffect potionEffect : potions.getArray(event)) {
				changeSafe(potionEffect, change, mode);
			}
			return;
		}

		//noinspection DataFlowIssue - not null by potions==null
		PotionEffectType[] types = this.types.getArray(event);
		//noinspection DataFlowIssue - not null by potions==null
		for (LivingEntity entity : entities.getArray(event)) {
			for (PotionEffectType type : types) {
				PotionEffect potionEffect = entity.getPotionEffect(type);
				if (potionEffect == null) {
					continue;
				}

				SkriptPotionEffect skriptPotionEffect = SkriptPotionEffect.fromBukkitEffect(potionEffect);
				changeSafe(skriptPotionEffect, change, mode);
				potionEffect = skriptPotionEffect.toPotionEffect();

				entity.removePotionEffect(type);
				entity.addPotionEffect(potionEffect);
			}
		}
	}

	private static void changeSafe(SkriptPotionEffect potionEffect, Timespan change, ChangeMode mode) {
		Timespan duration;
		if (mode == ChangeMode.SET || mode == ChangeMode.RESET) {
			duration = change;
		} else {
			int base = potionEffect.duration();
			if (base == PotionEffect.INFINITE_DURATION) {
				base = Integer.MAX_VALUE;
			}
			duration = new Timespan(TimePeriod.TICK, base);
			if (mode == ChangeMode.ADD) {
				duration = duration.add(change);
			} else {
				duration = duration.subtract(change);
			}
		}
		potionEffect.duration((int) Math2.fit(0, duration.getAs(TimePeriod.TICK), Integer.MAX_VALUE));
	}

	@Override
	public boolean isSingle() {
		if (potions != null) {
			return potions.isSingle();
		}
		//noinspection DataFlowIssue - not null by potions==null
		return types.isSingle() && entities.isSingle();
	}

	@Override
	public Class<? extends Timespan> getReturnType() {
		return Timespan.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append("the duration of");
		if (potions != null) {
			builder.append(potions);
		} else {
			//noinspection DataFlowIssue - not null by potions==null
			builder.append(types, "for", entities);
		}
		return builder.toString();
	}

}
