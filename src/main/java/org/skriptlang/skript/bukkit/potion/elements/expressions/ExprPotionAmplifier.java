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
import ch.njol.util.Kleenean;
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.ArrayList;
import java.util.List;

@Name("Potion Effect - Amplifier")
@Description("An expression to obtain the amplifier of a potion effect.")
@Example("set the amplifier of {_potion} to 10")
@Example("add 10 to the amplifier of speed for all players")
@Since("2.7, INSERT VERSION (support for potion effect objects, changing)")
public class ExprPotionAmplifier extends SimpleExpression<Integer> {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprPotionAmplifier.class, Integer.class)
			.priority(PropertyExpression.DEFAULT_PRIORITY)
			.addPatterns(
				"[the] [potion] (amplifier|tier|level)[s] of %skriptpotioneffects%",
				"%skriptpotioneffects%'[s] [potion] (amplifier|tier|level)[s]",
				"[the] [potion] (amplifier|tier)[s] of %potioneffecttypes% (of|for|on) %livingentities%"
			)
			.build()
		);
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
	protected Integer @Nullable [] get(Event event) {
		List<Integer> amplifiers = new ArrayList<>();
		if (potions != null) {
			for (SkriptPotionEffect potionEffect : potions.getArray(event)) {
				amplifiers.add(potionEffect.amplifier() + 1);
			}
		} else {
			//noinspection DataFlowIssue - not null by potions==null
			PotionEffectType[] types = this.types.getArray(event);
			//noinspection DataFlowIssue - not null by potions==null
			for (LivingEntity entity : entities.getArray(event)) {
				for (PotionEffectType type : types) {
					PotionEffect potionEffect = entity.getPotionEffect(type);
					amplifiers.add(potionEffect != null ? potionEffect.getAmplifier() + 1 : 0);
				}
			}
		}
		return amplifiers.toArray(new Integer[0]);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case ADD, SET, REMOVE -> CollectionUtils.array(Integer.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		assert delta != null;
		int change = (int) delta[0];

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

	private static void changeSafe(SkriptPotionEffect potionEffect, int change, ChangeMode mode) {
		// need to subtract 1 for setting
		int base = mode == ChangeMode.SET ? -1 : potionEffect.amplifier();
		if (mode == ChangeMode.REMOVE) {
			change = -change;
		}
		potionEffect.amplifier(Math2.fit(Integer.MIN_VALUE, change + base, Integer.MAX_VALUE));
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
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append("the amplifier of");
		if (potions != null) {
			builder.append(potions);
		} else {
			//noinspection DataFlowIssue - not null by potions==null
			builder.append(types, "for", entities);
		}
		return builder.toString();
	}

}
