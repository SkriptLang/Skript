package org.skriptlang.skript.bukkit.potion.providers;

import ch.njol.skript.aliases.ItemType;
import io.papermc.paper.potion.SuspiciousEffectEntry;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SuspiciousStewMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class ItemTypeProvider extends PotionEffectProvider<ItemType> {

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

	public ItemTypeProvider(ItemType source) {
		super(source);
	}

	@Override
	public Collection<SkriptPotionEffect> get(PotionEffectType[] potionEffectTypes, RetrievalState state) {
		if (!state.includesActive()) {
			return List.of();
		}
		List<SkriptPotionEffect> potionEffects = new ArrayList<>();
		for (PotionEffect effect : getPotionEffects(source)) {
			for (PotionEffectType type : potionEffectTypes) {
				if (type.equals(effect.getType())) {
					potionEffects.add(SkriptPotionEffect.fromBukkitEffect(effect, this));
					break;
				}
			}
		}
		return potionEffects;
	}

	@Override
	public Collection<SkriptPotionEffect> getAll(RetrievalState state) {
		if (!state.includesActive()) {
			return List.of();
		}
		return getPotionEffects(source).stream()
			.map(potionEffect -> SkriptPotionEffect.fromBukkitEffect(potionEffect, this))
			.toList();
	}

	@Override
	public void add(PotionEffect potionEffect) {
		addPotionEffects(source, potionEffect);
	}

	@Override
	public void remove(SkriptPotionEffect potionEffect, RetrievalState state) {
		if (!state.includesActive()) {
			return;
		}
		for (PotionEffect itemEffect : getPotionEffects(source)) {
			if (potionEffect.matchesQualities(itemEffect)) {
				removePotionEffects(source, potionEffect.potionEffectType());
				break; // API doesn't support multiple effects of the same type
			}
		}
	}

	@Override
	public void removeAll(PotionEffectType potionEffectType,  RetrievalState state) {
		if (!state.includesActive()) {
			return;
		}
		removePotionEffects(source, potionEffectType);
	}

	@Override
	public void clear(PotionEffectType[] potionEffectTypes, RetrievalState state) {
		if (!state.includesActive()) {
			return;
		}
		removePotionEffects(source, potionEffectTypes);
	}

	@Override
	public void clearAll(RetrievalState state) {
		if (!state.includesActive()) {
			return;
		}
		clearPotionEffects(source);
	}

	@Override
	public void mirrorEffectChanges(SkriptPotionEffect potionEffect, Runnable runnable) {
		removePotionEffects(source, potionEffect.potionEffectType());
		runnable.run();
		addPotionEffects(source, potionEffect.asBukkitPotionEffect());
	}

}
