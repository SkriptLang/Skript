/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package org.skriptlang.skript.potion.util;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.localization.Language;
import ch.njol.skript.util.Timespan;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SuspiciousStewMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PotionUtils {

	/**
	 * 30 seconds is the default length for the /effect command
	 * https://minecraft.fandom.com/wiki/Commands/effect
	 */
	public static final int DEFAULT_DURATION_TICKS = 600;

	private static final boolean HAS_SUSPICIOUS_META = Skript.classExists("org.bukkit.inventory.meta.SuspiciousStewMeta");

	static final Map<String, PotionEffectType> types = new HashMap<>();
	static final Map<PotionEffectType, String> names = new HashMap<>();

	static {
		Language.addListener(() -> {
			types.clear();
			for (PotionEffectType potionEffectType : PotionEffectType.values()) {
				String[] ls = Language.getList("potions." + potionEffectType.getName());
				names.put(potionEffectType, ls[0]);
				for (String l : ls)
					types.put(l.toLowerCase(), potionEffectType);
			}
		});
	}

	public static String[] getNames() {
		return names.values().toArray(new String[0]);
	}

	@Nullable
	public static PotionEffectType fromString(String s) {
		return types.get(s.toLowerCase());
	}

	public static String toString(PotionEffectType potionEffectType) {
		return toString(potionEffectType, 0);
	}

	// TODO flags
	public static String toString(PotionEffectType potionEffectType, int flags) {
		return names.get(potionEffectType);
	}

	public static String toString(PotionEffect potionEffect) {
		StringBuilder builder = new StringBuilder();
		if (potionEffect.isAmbient())
			builder.append("ambient ");
		builder.append("potion effect of ");
		builder.append(toString(potionEffect.getType()));
		builder.append(" of tier ").append(potionEffect.getAmplifier() + 1);
		if (!potionEffect.hasParticles())
			builder.append(" without particles");
		builder.append(" for ").append(Timespan.fromTicks_i(potionEffect.getDuration()));
		return builder.toString();
	}

	/**
	 * Converts an array of SkriptPotionEffects into an array of Bukkit PotionEffects.
	 * @param potionEffects The potion effects to convert.
	 * @return The converted potion effects.
	 */
	public static PotionEffect[] convertSkriptPotionEffects(SkriptPotionEffect[] potionEffects) {
		PotionEffect[] convertedEffects = new PotionEffect[potionEffects.length];
		for (int i = 0; i < convertedEffects.length; i++)
			convertedEffects[i] = potionEffects[i].toPotionEffect();
		return convertedEffects;
	}

	/**
	 * Converts a collection of SkriptPotionEffects into a list of Bukkit PotionEffects.
	 * @param potionEffects The potion effects to convert.
	 * @return The converted potion effects.
	 */
	public static List<PotionEffect> convertSkriptPotionEffects(Collection<SkriptPotionEffect> potionEffects) {
		List<PotionEffect> convertedEffects = new ArrayList<>();
		for (SkriptPotionEffect potionEffect : potionEffects)
			convertedEffects.add(potionEffect.toPotionEffect());
		return convertedEffects;
	}

	/**
	 * Converts an array of Bukkit PotionEffects into an array of SkriptPotionEffects.
	 * @param potionEffects The potion effects to convert.
	 * @return The converted potion effects.
	 */
	public static SkriptPotionEffect[] convertBukkitPotionEffects(PotionEffect[] potionEffects) {
		SkriptPotionEffect[] convertedEffects = new SkriptPotionEffect[potionEffects.length];
		for (int i = 0; i < convertedEffects.length; i++) {
			convertedEffects[i] = new SkriptPotionEffect(potionEffects[i]);
		}
		return convertedEffects;
	}

	/**
	 * Converts a collection of Bukkit PotionEffects into a list of SkriptPotionEffects.
	 * @param potionEffects The potion effects to convert.
	 * @return The converted potion effects.
	 */
	public static List<SkriptPotionEffect> convertBukkitPotionEffects(Collection<PotionEffect> potionEffects) {
		List<SkriptPotionEffect> convertedEffects = new ArrayList<>();
		for (PotionEffect potionEffect : potionEffects)
			convertedEffects.add(new SkriptPotionEffect(potionEffect));
		return convertedEffects;
	}

	/**
	 * Attempts to retrieve a list of potion effects from an ItemType.
	 * @param itemType The ItemType to get potion effects from.
	 * @return A list of potion effects from an ItemType, if any were found.
	 */
	public static List<PotionEffect> getPotionEffects(ItemType itemType) {
		List<PotionEffect> effects = new ArrayList<>();
		ItemMeta meta = itemType.getItemMeta();
		if (meta instanceof PotionMeta) {
			PotionMeta potionMeta = ((PotionMeta) meta);
			if (potionMeta.hasCustomEffects())
				effects.addAll(potionMeta.getCustomEffects());
			effects.addAll(PotionDataUtils.getPotionEffects(potionMeta.getBasePotionData()));
		} else if (HAS_SUSPICIOUS_META && meta instanceof SuspiciousStewMeta) {
			effects.addAll(((SuspiciousStewMeta) meta).getCustomEffects());
		}
		return effects;
	}

	/**
	 * Adds potions effects to an ItemType.
	 * @param itemType The ItemType to modify.
	 * @param potionEffects The potion effects to add.
	 */
	public static void addPotionEffects(ItemType itemType, PotionEffect... potionEffects) {
		ItemMeta meta = itemType.getItemMeta();
		for (PotionEffect potionEffect : potionEffects) {
			if (meta instanceof PotionMeta) {
				((PotionMeta) meta).addCustomEffect(potionEffect, false);
			} else if (HAS_SUSPICIOUS_META && meta instanceof SuspiciousStewMeta) {
				((SuspiciousStewMeta) meta).addCustomEffect(potionEffect, false);
			}
		}
		itemType.setItemMeta(meta);
	}

	/**
	 * Removes potions effects from an ItemType.
	 * @param itemType The ItemType to modify.
	 * @param potionEffectTypes The potion effects to remove.
	 */
	public static void removePotionEffects(ItemType itemType, PotionEffectType... potionEffectTypes) {
		ItemMeta meta = itemType.getItemMeta();
		for (PotionEffectType potionEffectType : potionEffectTypes) {
			if (meta instanceof PotionMeta) {
				((PotionMeta) meta).removeCustomEffect(potionEffectType);
			} else if (HAS_SUSPICIOUS_META && meta instanceof SuspiciousStewMeta) {
				((SuspiciousStewMeta) meta).removeCustomEffect(potionEffectType);
			}
		}
		itemType.setItemMeta(meta);
	}

	/**
	 * Removes all potion effects from the ItemType's meta.
	 * @param itemType The ItemType to modify.
	 */
	public static void clearPotionEffects(ItemType itemType) {
		ItemMeta meta = itemType.getItemMeta();
		if (meta instanceof PotionMeta) {
			((PotionMeta) meta).clearCustomEffects();
		} else if (HAS_SUSPICIOUS_META && meta instanceof SuspiciousStewMeta) {
			((SuspiciousStewMeta) meta).clearCustomEffects();
		}
		itemType.setItemMeta(meta);
	}

}
