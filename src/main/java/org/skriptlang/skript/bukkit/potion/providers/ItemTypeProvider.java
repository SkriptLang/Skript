package org.skriptlang.skript.bukkit.potion.providers;

import ch.njol.skript.aliases.ItemType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.skriptlang.skript.bukkit.potion.util.PotionUtils;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class ItemTypeProvider extends PotionEffectProvider<ItemType> {

	public ItemTypeProvider(ItemType source) {
		super(source);
	}

	@Override
	public Collection<SkriptPotionEffect> get(PotionEffectType[] potionEffectTypes, RetrievalState state) {
		if (!state.includesActive()) {
			return List.of();
		}
		List<SkriptPotionEffect> potionEffects = new ArrayList<>();
		for (PotionEffect effect : PotionUtils.getPotionEffects(source)) {
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
		return PotionUtils.getPotionEffects(source).stream()
			.map(potionEffect -> SkriptPotionEffect.fromBukkitEffect(potionEffect, this))
			.toList();
	}

	@Override
	public void add(PotionEffect potionEffect) {
		PotionUtils.addPotionEffects(source, potionEffect);
	}

	@Override
	public void remove(SkriptPotionEffect potionEffect, RetrievalState state) {
		for (PotionEffect itemEffect : PotionUtils.getPotionEffects(source)) {
			if (potionEffect.matchesQualities(itemEffect)) {
				PotionUtils.removePotionEffects(source, potionEffect.potionEffectType());
				break; // API doesn't support multiple effects of the same type
			}
		}
	}

	@Override
	public void removeAll(PotionEffectType potionEffectType) {
		PotionUtils.removePotionEffects(source, potionEffectType);
	}

	@Override
	public void clear(PotionEffectType[] potionEffectTypes, RetrievalState state) {
		PotionUtils.removePotionEffects(source, potionEffectTypes);
	}

	@Override
	public void clearAll(RetrievalState state) {
		PotionUtils.clearPotionEffects(source);
	}

	@Override
	public void mirrorEffectChanges(SkriptPotionEffect potionEffect, Runnable runnable) {
		PotionUtils.removePotionEffects(source, potionEffect.potionEffectType());
		runnable.run();
		PotionUtils.addPotionEffects(source, potionEffect.asBukkitPotionEffect());
	}

}
