package org.skriptlang.skript.bukkit.potion.util;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import io.papermc.paper.potion.SuspiciousEffectEntry;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SuspiciousStewMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public final class PotionUtils {

	/**
	 * 30 seconds is the default length for the /effect command
	 * See <a href="https://minecraft.wiki/w/Commands/effect">https://minecraft.wiki/w/Commands/effect</a>
	 */
	public static final int DEFAULT_DURATION_TICKS = 600;
	/**
	 * A string representation of a {@link Timespan} of {@link #DEFAULT_DURATION_TICKS}.
	 */
	public static final String DEFAULT_DURATION_STRING = new Timespan(TimePeriod.TICK, DEFAULT_DURATION_TICKS).toString();

	/**
	 * Attempts to retrieve a list of potion effects from an ItemType.
	 * @param itemType The ItemType to get potion effects from.
	 * @return A list of potion effects from an ItemType, if any were found.
	 */
	public static List<PotionEffect> getPotionEffects(ItemType itemType) {
		List<PotionEffect> effects = new ArrayList<>();
		ItemMeta meta = itemType.getItemMeta();
		if (meta instanceof PotionMeta potionMeta) {
			if (potionMeta.hasCustomEffects()) {
				effects.addAll(potionMeta.getCustomEffects());
			}
			if (potionMeta.hasBasePotionType()) {
				//noinspection ConstantConditions - checked via hasBasePotionType
				effects.addAll(potionMeta.getBasePotionType().getPotionEffects());
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
				stewMeta.addCustomEffect(
					SuspiciousEffectEntry.create(potionEffect.getType(), potionEffect.getDuration()), true);
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

	/**
	 * A utility method to obtain the hidden effects of a potion effect.
	 * @param effect The effect to obtain hidden effects from.
	 * @return A deque of the hidden effects of {@code effect} ordered from most hidden to least hidden.
	 */
	public static Deque<PotionEffect> getHiddenEffects(PotionEffect effect) {
		Deque<PotionEffect> hiddenEffects = new ArrayDeque<>();

		// build hidden effects chain to reapply
		PotionEffect hiddenEffect = effect.getHiddenPotionEffect();
		while (hiddenEffect != null) {
			hiddenEffects.push(hiddenEffect);
			hiddenEffect = hiddenEffect.getHiddenPotionEffect();
		}

		return hiddenEffects;
	}

}
