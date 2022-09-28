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

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.skriptlang.skript.bukkit.potion.util.PotionUtils;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

@Name("Potion Effects")
@Description({
		"Returns the active potion effects of entities or items.",
		"You can clear all potion effects of an entity or item and add or remove a potion effect to/from an entity or item.",
		"Please note that you can't clear the base potion effects of a potion item. If you wish to do so, just set the item to a water bottle.",
		"Also note that you can't modify the properties of these potion effects, as they have already been applied."
})
@Examples({
		"set {_p::*} to the active potion effects of player",
		"clear the potion effects of player",
		"clear the potion effects of player's tool",
		"add potion effects of player to potion effects of player's tool",
		"add speed to potion effects of target entity",
		"remove speed and night vision from potion effects of player"
})
@Since("2.5.2, INSERT VERSION (syntax changes, reset support)")
public class ExprPotionEffects extends PropertyExpression<Object, SkriptPotionEffect> {
	
	static {
		register(ExprPotionEffects.class, SkriptPotionEffect.class, "[active] potion effects", "livingentities/itemtypes");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr(exprs[0]);
		return true;
	}

	@Override
	protected SkriptPotionEffect[] get(Event e, Object[] source) {
		List<SkriptPotionEffect> effects = new ArrayList<>();
		for (Object object : source) {
			if (object instanceof LivingEntity) {
				effects.addAll(PotionUtils.convertBukkitPotionEffects(((LivingEntity) object).getActivePotionEffects()));
			} else if (object instanceof ItemType) {
				effects.addAll(PotionUtils.convertBukkitPotionEffects(PotionUtils.getPotionEffects((ItemType) object)));
			}
		}
		return effects.toArray(new SkriptPotionEffect[0]);
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		switch (mode) {
			case REMOVE:
				return CollectionUtils.array(SkriptPotionEffect[].class, PotionEffectType[].class);
			case ADD:
			case DELETE:
			case RESET:
				return CollectionUtils.array(SkriptPotionEffect[].class);
			default:
				return null;
		}
	}
	
	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		if ((delta == null || delta.length == 0) && mode != ChangeMode.DELETE)
			return;

		Object[] holders = getExpr().getArray(e);

		switch (mode) {
			case ADD:
				for (Object holder : holders) {
					PotionEffect[] convertedEffects = PotionUtils.convertSkriptPotionEffects((SkriptPotionEffect[]) delta);
					if (holder instanceof LivingEntity) {
						for (PotionEffect potionEffect : convertedEffects)
							((LivingEntity) holder).addPotionEffect(potionEffect);
					} else if (holder instanceof ItemType) {
						PotionUtils.addPotionEffects((ItemType) holder, convertedEffects);
					}
				}
				break;
			case REMOVE:
				for (Object holder : holders) {
					if (holder instanceof LivingEntity) {
						for (Object potionEffect : delta) {
							if (potionEffect instanceof SkriptPotionEffect) {
								((LivingEntity) holder).removePotionEffect(((SkriptPotionEffect) potionEffect).potionEffectType());
							} else if (potionEffect instanceof PotionEffectType) {
								((LivingEntity) holder).removePotionEffect((PotionEffectType) potionEffect);
							}
						}
					} else if (holder instanceof ItemType) {
						PotionEffectType[] potionEffectTypes = new PotionEffectType[delta.length];
						for (int i = 0; i < potionEffectTypes.length; i++) {
							Object potionEffect = delta[i];
							if (potionEffect instanceof SkriptPotionEffect) {
								potionEffectTypes[i] = ((SkriptPotionEffect) potionEffect).potionEffectType();
							} else {
								assert potionEffect instanceof PotionEffectType;
								potionEffectTypes[i] = (PotionEffectType) potionEffect;
							}
						}
						PotionUtils.removePotionEffects((ItemType) holder, potionEffectTypes);
					}
				}
				break;
			case DELETE:
			case RESET:
				for (Object holder : holders) {
					if (holder instanceof LivingEntity) {
						((LivingEntity) holder).getActivePotionEffects().forEach(potionEffect -> ((LivingEntity) holder).removePotionEffect(potionEffect.getType()));
					} else if (holder instanceof ItemType) {
						PotionUtils.clearPotionEffects((ItemType) holder);
					}
				}
				break;
			default:
				assert false;
		}
	}

	@Override
	public Class<? extends SkriptPotionEffect> getReturnType() {
		return SkriptPotionEffect.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "active potion effects of " + getExpr().toString(e, debug);
	}

}
