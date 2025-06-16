package org.skriptlang.skript.bukkit.potion.elements.effects;

import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;

abstract class PotionPropertyEffect extends Effect {

	public enum Type {
		MAKE,
		SHOW
	}

	public static String[] getPatterns(Type type, String property) {
		return switch (type) {
			case MAKE -> new String[]{
				"make %skriptpotioneffects% [:not] " + property,
				"make %potioneffecttypes% (of|for|on) %livingentities% [:not] " + property
			};
			case SHOW -> new String[]{
				"(show|not:hide) [the] [potion] " + property + " [(of|for) %skriptpotioneffects%]",
				"(show|not:hide) %skriptpotioneffects%'[s] " + property,
				"(show|not:hide) [the] [potion] " + property + " (of|for) %potioneffecttypes% (of|for|on) %livingentities%"
			};
		};
	}

	private @Nullable Expression<SkriptPotionEffect> potions;
	private @Nullable Expression<PotionEffectType> types;
	private @Nullable Expression<LivingEntity> entities;

	private boolean isNegated;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (expressions.length == 1) {
			potions = (Expression<SkriptPotionEffect>) expressions[0];
		} else {
			types = (Expression<PotionEffectType>) expressions[0];
			entities = (Expression<LivingEntity>) expressions[1];
		}
		isNegated = parseResult.hasTag("not");
		return true;
	}

	@Override
	protected void execute(Event event) {
		if (potions != null) {
			for (SkriptPotionEffect potionEffect : potions.getArray(event)) {
				modify(potionEffect, isNegated);
			}
			return;
		}

		//noinspection DataFlowIssue - not null by potions==null
		PotionEffectType[] types = this.types.getArray(event);
		//noinspection DataFlowIssue - not null by potions==null
		for (LivingEntity entity : entities.getArray(event)) {
			for (PotionEffectType type : types) {
				PotionEffect potionEffect = entity.getPotionEffect(type);
				if (potionEffect == null)
					continue;

				SkriptPotionEffect skriptPotionEffect = SkriptPotionEffect.fromBukkitEffect(potionEffect);
				modify(skriptPotionEffect, isNegated);
				potionEffect = skriptPotionEffect.toPotionEffect();

				entity.removePotionEffect(type);
				entity.addPotionEffect(potionEffect);
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		switch (getPropertyType()) {
			case MAKE -> {
				builder.append("make");
				if (potions != null) {
					builder.append(potions);
				} else {
					//noinspection DataFlowIssue - not null by potions==null
					builder.append(types, "for", entities);
				}
				if (isNegated) {
					builder.append("not");
				}
				builder.append(getPropertyName());
			}
			case SHOW -> {
				builder.append(isNegated ? "hide" : "show", "the potion", getPropertyName(), "of");
				if (potions != null) {
					builder.append(potions);
				} else {
					//noinspection DataFlowIssue - not null by potions==null
					builder.append(types, "for", entities);
				}

			}
		}
		return builder.toString();
	}

	public abstract void modify(SkriptPotionEffect effect, boolean isNegated);
	public abstract Type getPropertyType();
	public abstract String getPropertyName();

}
