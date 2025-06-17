package org.skriptlang.skript.bukkit.potion.util;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.BukkitUtils;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import org.bukkit.Registry;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SuspiciousStewMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class PotionUtils {

	/**
	 * Changes to the PotionEffectType enum occurred in 1.20.5 which resulted in renaming of several elements
	 */
	static final boolean HAS_OLD_POTION_FIELDS = Skript.fieldExists(PotionEffectType.class, "SLOW");

	/**
	 * 30 seconds is the default length for the /effect command
	 * See <a href="https://minecraft.fandom.com/wiki/Commands/effect">https://minecraft.fandom.com/wiki/Commands/effect</a>
	 */
	public static final int DEFAULT_DURATION_TICKS = 600;
	/**
	 * A string representation of a {@link Timespan} of {@link #DEFAULT_DURATION_TICKS}.
	 */
	public static final String DEFAULT_DURATION_STRING = new Timespan(TimePeriod.TICK, DEFAULT_DURATION_TICKS).toString();

	private static final boolean HAS_GET_POTION_TYPE_METHOD = Skript.methodExists(PotionMeta.class, "getBasePotionType");
	private static final boolean HAS_HAS_POTION_TYPE_METHOD = Skript.methodExists(PotionMeta.class, "hasBasePotionType");

	/**
	 * A convenience method for obtaining the Registry representing PotionEffectTypes,
	 * as the API has two different names for the same registry.
	 *
	 * @return Registry for PotionEffectType
	 */
	@SuppressWarnings("NullableProblems")
	public static @Nullable Registry<PotionEffectType> getPotionEffectTypeRegistry() {
		if (BukkitUtils.registryExists("MOB_EFFECT")) { // Paper (1.21.4)
			return Registry.MOB_EFFECT;
		} else if (BukkitUtils.registryExists("EFFECT")) { // Bukkit (1.20.3)
			return Registry.EFFECT;
		}
		return null;
	}

	/**
	 * Attempts to retrieve a list of potion effects from an ItemType.
	 * @param itemType The ItemType to get potion effects from.
	 * @return A list of potion effects from an ItemType, if any were found.
	 */
	public static List<PotionEffect> getPotionEffects(ItemType itemType) {
		List<PotionEffect> effects = new ArrayList<>();
		ItemMeta meta = itemType.getItemMeta();
		if (meta instanceof PotionMeta potionMeta) {
			if (potionMeta.hasCustomEffects())
				effects.addAll(potionMeta.getCustomEffects());
			if (HAS_GET_POTION_TYPE_METHOD) {
				if (HAS_HAS_POTION_TYPE_METHOD) { // Not available on all versions where getBasePotionType exists
					if (potionMeta.hasBasePotionType())
						//noinspection ConstantConditions - checked via hasBasePotionType
						effects.addAll(potionMeta.getBasePotionType().getPotionEffects());
				} else {
					PotionType potionType = potionMeta.getBasePotionType();
					if (potionType != null)
						effects.addAll(potionType.getPotionEffects());
				}
			} else {
				//noinspection deprecation - Compatibility measure
				PotionData potionData = potionMeta.getBasePotionData();
				if (potionData != null)
					effects.addAll(PotionDataUtils.getPotionEffects(potionData));
			}
		} else if (meta instanceof SuspiciousStewMeta stewMeta) {
			effects.addAll(stewMeta.getCustomEffects());
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
		if (meta instanceof PotionMeta potionMeta) {
			for (PotionEffect potionEffect : potionEffects) {
				potionMeta.addCustomEffect(potionEffect, true);
			}
		} else if (meta instanceof SuspiciousStewMeta stewMeta) {
			for (PotionEffect potionEffect : potionEffects) {
				stewMeta.addCustomEffect(potionEffect, true);
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
		if (meta instanceof PotionMeta potionMeta) {
			for (PotionEffectType potionEffectType : potionEffectTypes) {
				potionMeta.removeCustomEffect(potionEffectType);
			}
		} else if (meta instanceof SuspiciousStewMeta stewMeta) {
			for (PotionEffectType potionEffectType : potionEffectTypes) {
				stewMeta.removeCustomEffect(potionEffectType);
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
		if (meta instanceof PotionMeta potionMeta) {
			potionMeta.clearCustomEffects();
		} else if (meta instanceof SuspiciousStewMeta stewMeta) {
			stewMeta.clearCustomEffects();
		}
		itemType.setItemMeta(meta);
	}

}
