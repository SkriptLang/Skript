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
package org.skriptlang.skriptbukkit.potion.util;

import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Bukkit does not provide a way to get a PotionEffect from PotionData
// This class allows us to convert base PotionData of an item into a PotionEffect
public enum PotionDataUtils {
	
	FIRE_RESISTANCE(PotionType.FIRE_RESISTANCE, false, false, 3600, 0),
	FIRE_RESISTANCE_LONG(PotionType.FIRE_RESISTANCE, true, false, 9600, 0),
	HARMING(PotionType.INSTANT_DAMAGE, false, false, 1, 0),
	HARMING_STRONG(PotionType.INSTANT_DAMAGE, false, true, 1, 1),
	HEALING(PotionType.INSTANT_HEAL, false, false, 1, 0),
	HEALING_STRONG(PotionType.INSTANT_HEAL, false, true, 1, 1),
	INVISIBILITY(PotionType.INVISIBILITY, false, false, 3600, 0),
	INVISIBILITY_LONG(PotionType.INVISIBILITY, true, false, 9600, 0),
	LEAPING(PotionType.JUMP, false, false, 3600, 0),
	LEAPING_LONG(PotionType.JUMP, true, false, 9600, 0),
	LEAPING_STRONG(PotionType.JUMP, false, true, 1800, 1),
	LUCK(PotionType.LUCK, false, false, 6000, 0),
	NIGHT_VISION(PotionType.NIGHT_VISION, false, false, 3600, 0),
	NIGHT_VISION_LONG(PotionType.NIGHT_VISION, true, false, 9600, 0),
	POISON(PotionType.POISON, false, false, 900, 0),
	POISON_LONG(PotionType.POISON, true, false, 1800, 0),
	POISON_STRONG(PotionType.POISON, false, true, 432, 1),
	REGENERATION(PotionType.REGEN, false, false, 900, 0),
	REGENERATION_LONG(PotionType.REGEN, true, false, 1800, 0),
	REGENERATION_STRONG(PotionType.REGEN, false, true, 450, 1),
	SLOW_FALLING(PotionType.SLOW_FALLING, false, false, 1800, 0),
	SLOW_FALLING_LONG(PotionType.SLOW_FALLING, true, false, 4800, 0),
	SLOWNESS(PotionType.SLOWNESS, false, false, 1800, 0),
	SLOWNESS_LONG(PotionType.SLOWNESS, true, false, 4800, 0),
	SLOWNESS_STRONG(PotionType.SLOWNESS, false, true, 400, 3),
	SWIFTNESS(PotionType.SPEED, false, false, 3600, 0),
	SWIFTNESS_LONG(PotionType.SPEED, true, false, 9600, 0),
	SWIFTNESS_STRONG(PotionType.SPEED, false, true, 1800, 1),
	STRENGTH(PotionType.STRENGTH, false, false, 3600, 0),
	STRENGTH_LONG(PotionType.STRENGTH, true, false, 9600, 0),
	STRENGTH_STRONG(PotionType.STRENGTH, false, true, 1800, 1),
	TURTLE_MASTER(PotionType.TURTLE_MASTER, false, false, 0, 0),
	TURTLE_MASTER_LONG(PotionType.TURTLE_MASTER, true, false, 0, 0),
	TURTLE_MASTER_STRONG(PotionType.TURTLE_MASTER, false, true, 0, 0),
	WATER_BREATHING(PotionType.WATER_BREATHING, false, false, 3600, 0),
	WATER_BREATHING_LONG(PotionType.WATER_BREATHING, true, false, 9600, 0),
	WEAKNESS(PotionType.WEAKNESS, false, false, 1800, 0),
	WEAKNESS_LONG(PotionType.WEAKNESS, false, false, 4800, 0);

	@Nullable
	private PotionType potionType;
	private final boolean extended;
	private final boolean upgraded;
	private final int duration;
	private final int amplifier;
	
	PotionDataUtils(PotionType potionType, boolean extended, boolean upgraded, int duration, int amplifier) {
		this.potionType = potionType;
		this.extended = extended;
		this.upgraded = upgraded;
		this.duration = duration;
		this.amplifier = amplifier;
	}
	
	PotionDataUtils(String potionType, boolean extended, boolean upgraded, int duration, int amplifier) {
		try {
			this.potionType = PotionType.valueOf(potionType.toUpperCase(Locale.ENGLISH));
		} catch (IllegalArgumentException ignore) {
			this.potionType = null;
		}
		this.extended = extended;
		this.upgraded = upgraded;
		this.duration = duration;
		this.amplifier = amplifier;
	}
	
	/**
	 * Convert {@link PotionData} to a {@link PotionEffect}
	 *
	 * @param potionData PotionData to convert
	 * @return List of PotionEffects from the data
	 */
	@SuppressWarnings("null")
	public static List<PotionEffect> getPotionEffects(PotionData potionData) {
		List<PotionEffect> potionEffects = new ArrayList<>();
		for (PotionDataUtils value : PotionDataUtils.values()) {
			if (value.potionType != null && potionData.getType() == value.potionType && potionData.isExtended() == value.extended && potionData.isUpgraded() == value.upgraded) {
				if (value.potionType == PotionType.TURTLE_MASTER) {
					// Bukkit does not account for the fact that Turtle Master has 2 potion effects
					int duration = value.extended ? 800 : 400;
					int slowAmp = value.upgraded ? 5 : 3;
					int resistanceAmp = value.upgraded ? 3 : 2;
					potionEffects.add(new PotionEffect(PotionEffectType.SLOW, duration, slowAmp, false));
					potionEffects.add(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, duration, resistanceAmp, false));
					continue;
				}
				PotionEffectType potionEffectType = value.potionType.getEffectType();
				if (potionEffectType == null)
					continue;
				potionEffects.add(new PotionEffect(potionEffectType, value.duration, value.amplifier, false));
			}
		}
		return potionEffects;
	}
	
}
