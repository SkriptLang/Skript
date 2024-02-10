/**
 * This file is part of Skript.
 *
 * Skript is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Skript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package org.skriptlang.skript.bukkit.potion.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.eclipse.jdt.annotation.Nullable;
import org.skriptlang.skript.bukkit.potion.util.PotionUtils;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect.Property;

import java.util.ArrayList;
import java.util.List;

@Name("Potion Properties")
@Description("An expression to obtain and modify properties of a potion such as the duration or amplifier.")
@Examples("if the amplifier of haste for the player >= 3:")
@Since("INSERT VERSION")
public class ExprPotionProperties extends SimpleExpression<Object> {

	static {
		String properties = "(AMPLIFIER:(tier|amplifier|level)|DURATION:(duration|length)|EFFECT:(type|effect [type]))";
		Skript.registerExpression(ExprPotionProperties.class, Object.class, ExpressionType.COMBINED,
				"[the] potion " + properties + " of %potioneffecttypes% (of|for|on) %livingentities%",
				"[the] potion " + properties + " of %potioneffects%",
				"%potioneffects%'[s] potion" + properties
		);
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Property property;

	@Nullable
	private Expression<PotionEffectType> types;
	@Nullable
	private Expression<LivingEntity> entities;
	@Nullable
	private Expression<SkriptPotionEffect> potions;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		property = Property.valueOf(parseResult.tags.get(0));
		if (matchedPattern == 0) {
			types = (Expression<PotionEffectType>) exprs[0];
			entities = (Expression<LivingEntity>) exprs[1];
		} else {
			potions = (Expression<SkriptPotionEffect>) exprs[0];
		}
		return true;
	}

	@Override
	@Nullable
	protected Object[] get(Event event) {
		List<Object> values;
		if (potions != null) {

			SkriptPotionEffect[] potionEffects = potions.getArray(event);
			values = new ArrayList<>(potionEffects.length);
			switch (property) {
				case AMPLIFIER:
					for (SkriptPotionEffect potionEffect : potionEffects)
						values.add(potionEffect.amplifier() + 1);
					break;
				case DURATION:
					for (SkriptPotionEffect potionEffect : potionEffects)
						values.add(Timespan.fromTicks(potionEffect.duration()));
					break;
				case EFFECT:
					for (SkriptPotionEffect potionEffect : potionEffects)
						values.add(potionEffect.potionEffectType());
					break;
			}

		} else {

			assert types != null && entities != null;
			PotionEffectType[] types = this.types.getArray(event);
			LivingEntity[] entities = this.entities.getArray(event);
			List<PotionEffect> potionEffects = new ArrayList<>(types.length * entities.length);
			for (LivingEntity entity : entities) {
				for (PotionEffectType type : types) {
					PotionEffect potionEffect = entity.getPotionEffect(type);
					if (potionEffect != null)
						potionEffects.add(potionEffect);
				}
			}

			values = new ArrayList<>(potionEffects.size());
			switch (property) {
				case AMPLIFIER:
					for (PotionEffect potionEffect : potionEffects)
						values.add(potionEffect.getAmplifier() + 1);
					break;
				case DURATION:
					for (PotionEffect potionEffect : potionEffects)
						values.add(Timespan.fromTicks(potionEffect.getDuration()));
					break;
				case EFFECT:
					for (PotionEffect potionEffect : potionEffects)
						values.add(potionEffect.getType());
					break;
			}

		}

		return values.toArray(new Object[0]);
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		switch (property) {
			case AMPLIFIER:
				switch (mode) {
					case SET:
					case ADD:
					case REMOVE:
						return CollectionUtils.array(Number.class);
					default:
						return null;
				}
			case DURATION:
				switch (mode) {
					case SET:
					case ADD:
					case REMOVE:
					case RESET:
						return CollectionUtils.array(Timespan.class);
					default:
						return null;
				}
			case EFFECT:
				return mode == ChangeMode.SET ? CollectionUtils.array(PotionEffectType.class) : null;
			default:
				throw new IllegalArgumentException("Unexpected Potion Property: " + property);
		}
	}

	@Override
	public void change(Event event, Object[] delta, ChangeMode mode) {
		if (potions != null) {
			SkriptPotionEffect[] potionEffects = potions.getArray(event);
			switch (property) {
				case AMPLIFIER:
					int change = ((Number) delta[0]).intValue();
					switch (mode) {
						case SET:
							for (SkriptPotionEffect potionEffect : potionEffects)
								potionEffect.amplifier(change);
							break;
						case ADD:
							for (SkriptPotionEffect potionEffect : potionEffects)
								potionEffect.amplifier(potionEffect.amplifier() + change);
							break;
						case REMOVE:
							for (SkriptPotionEffect potionEffect : potionEffects)
								potionEffect.amplifier(potionEffect.amplifier() - change);
							break;
					}
					break;
				case DURATION:
					int ticks = delta != null ? (int) ((Timespan) delta[0]).getTicks() : PotionUtils.DEFAULT_DURATION_TICKS;
					switch (mode) {
						case SET:
						case RESET:
							for (SkriptPotionEffect potionEffect : potionEffects)
								potionEffect.duration(ticks);
							break;
						case ADD:
							for (SkriptPotionEffect potionEffect : potionEffects)
								potionEffect.duration(Math.max(0, potionEffect.duration() + ticks));
							break;
						case REMOVE:
							for (SkriptPotionEffect potionEffect : potionEffects)
								potionEffect.duration(Math.max(0, potionEffect.duration() - ticks));
							break;
					}
					break;
				case EFFECT:
					PotionEffectType type = (PotionEffectType) delta[0];
					for (SkriptPotionEffect potionEffect : potionEffects)
						potionEffect.potionEffectType(type);
					break;
				default:
					throw new IllegalArgumentException("Unexpected Potion Property: " + property);
			}
		} else {
			assert types != null && entities != null;
			PotionEffectType[] types = this.types.getArray(event);
			LivingEntity[] entities = this.entities.getArray(event);
			switch (property) {
				case AMPLIFIER:
					int change = ((Number) delta[0]).intValue();
					for (LivingEntity entity : entities) {
						for (PotionEffectType type : types) {
							PotionEffect potionEffect = entity.getPotionEffect(type);
							if (potionEffect == null)
								continue;

							int newAmplifier = change;
							if (mode == ChangeMode.ADD)
								newAmplifier = potionEffect.getAmplifier() + change;
							if (mode == ChangeMode.REMOVE)
								newAmplifier = potionEffect.getAmplifier() - change;

							entity.removePotionEffect(type);
							entity.addPotionEffect(new SkriptPotionEffect(potionEffect).amplifier(newAmplifier).toPotionEffect());
						}
					}
					break;
				case DURATION:
					int ticks = delta != null ? (int) ((Timespan) delta[0]).getTicks() : PotionUtils.DEFAULT_DURATION_TICKS;
					for (LivingEntity entity : entities) {
						for (PotionEffectType type : types) {
							PotionEffect potionEffect = entity.getPotionEffect(type);
							if (potionEffect == null)
								continue;

							int newDuration = ticks;
							if (mode == ChangeMode.ADD)
								newDuration = potionEffect.getDuration() + newDuration;
							if (mode == ChangeMode.REMOVE)
								newDuration = potionEffect.getDuration() - newDuration;
							newDuration = Math.max(0, newDuration);

							entity.removePotionEffect(type);
							entity.addPotionEffect(new SkriptPotionEffect(potionEffect).duration(newDuration).toPotionEffect());
						}
					}
					break;
				case EFFECT:
					PotionEffectType newType = (PotionEffectType) delta[0];
					for (LivingEntity entity : entities) {
						for (PotionEffectType type : types) {
							PotionEffect potionEffect = entity.getPotionEffect(type);
							if (potionEffect == null)
								continue;
							entity.removePotionEffect(type);
							entity.removePotionEffect(newType);
							entity.addPotionEffect(new SkriptPotionEffect(potionEffect).potionEffectType(newType).toPotionEffect());
						}
					}
					break;
				default:
					throw new IllegalArgumentException("Unexpected Potion Property: " + property);
			}
		}
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<?> getReturnType() {
		switch (property) {
			case AMPLIFIER:
				return Integer.class;
			case DURATION:
				return Timespan.class;
			case EFFECT:
				return PotionEffectType.class;
			default:
				throw new IllegalArgumentException("Unexpected Potion Property: " + property);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (potions != null) {
			return "the " + property.displayName() + " of " + potions.toString(event, debug);
		}
		assert types != null && entities != null;
		return "the potion " + property.displayName() + " of " + types.toString(event, debug) + " for " + entities.toString(event, debug);
	}

}
