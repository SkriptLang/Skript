package org.skriptlang.skript.bukkit.potion.providers;

import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Arrow;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.potion.providers.EntityProvider.PotionAccessor;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class EntityProvider extends PotionEffectProvider<PotionAccessor> {

	public EntityProvider(AreaEffectCloud areaEffectCloud) {
		super(new AreaEffectCloudAccessor(areaEffectCloud));
	}

	public EntityProvider(Arrow arrow) {
		super(new ArrowAccessor(arrow));
	}

	@Override
	public Collection<SkriptPotionEffect> get(PotionEffectType[] potionEffectTypes, RetrievalState state) {
		if (!state.includesActive()) {
			return List.of();
		}
		List<PotionEffect> potionEffects = new ArrayList<>();
		PotionType potionType = source.getBasePotionType();
		if (potionType != null) {
			potionEffects.addAll(potionType.getPotionEffects());
		}
		if (source.hasCustomEffects()) {
			potionEffects.addAll(source.getCustomEffects());
		}
		return potionEffects.stream()
			.filter(effect -> {
				for (PotionEffectType type : potionEffectTypes) {
					if (type.equals(effect.getType())) {
						return true;
					}
				}
				return false;
			})
			.map(effect -> SkriptPotionEffect.fromBukkitEffect(effect, this))
			.toList();
	}

	@Override
	public Collection<SkriptPotionEffect> getAll(RetrievalState state) {
		if (!state.includesActive()) {
			return List.of();
		}
		List<SkriptPotionEffect> potionEffects = new ArrayList<>();
		PotionType potionType = source.getBasePotionType();
		if (potionType != null) {
			for (PotionEffect effect : potionType.getPotionEffects()) {
				potionEffects.add(SkriptPotionEffect.fromBukkitEffect(effect, this));
			}
		}
		if (source.hasCustomEffects()) {
			for (PotionEffect effect : source.getCustomEffects()) {
				potionEffects.add(SkriptPotionEffect.fromBukkitEffect(effect, this));
			}
		}
		return potionEffects;
	}

	@Override
	public void add(PotionEffect potionEffect) {
		source.addCustomEffect(potionEffect, true);
	}

	@Override
	public void remove(SkriptPotionEffect potionEffect, RetrievalState state) {
		if (!state.includesActive() || !source.hasCustomEffects()) {
			return;
		}
		for (PotionEffect itemEffect : source.getCustomEffects()) {
			if (potionEffect.matchesQualities(itemEffect)) {
				source.removeCustomEffect(potionEffect.potionEffectType());
				break; // API doesn't support multiple effects of the same type
			}
		}
	}

	@Override
	public void removeAll(PotionEffectType potionEffectType, RetrievalState state) {
		if (!state.includesActive()) {
			return;
		}
		source.removeCustomEffect(potionEffectType);
	}

	@Override
	public void clear(PotionEffectType[] potionEffectTypes, RetrievalState state) {
		if (!state.includesActive()) {
			return;
		}
		for (PotionEffectType potionEffectType : potionEffectTypes) {
			source.removeCustomEffect(potionEffectType);
		}
	}

	@Override
	public void clearAll(RetrievalState state) {
		if (!state.includesActive()) {
			return;
		}
		source.clearCustomEffects();
	}

	@Override
	public void mirrorEffectChanges(SkriptPotionEffect potionEffect, Runnable runnable) {
		source.removeCustomEffect(potionEffect.potionEffectType());
		runnable.run();
		source.addCustomEffect(potionEffect.asBukkitPotionEffect(), true);
	}

	public interface PotionAccessor {
		@Nullable PotionType getBasePotionType();
		boolean hasCustomEffects();
		List<PotionEffect> getCustomEffects();
		void addCustomEffect(PotionEffect effect, boolean overwrite);
		void removeCustomEffect(PotionEffectType potionEffectType);
		void clearCustomEffects();
	}

	private record AreaEffectCloudAccessor(AreaEffectCloud areaEffectCloud) implements PotionAccessor {

		@Override
		public @Nullable PotionType getBasePotionType() {
			return areaEffectCloud.getBasePotionType();
		}

		@Override
		public boolean hasCustomEffects() {
			return areaEffectCloud.hasCustomEffects();
		}

		@Override
		public List<PotionEffect> getCustomEffects() {
			return areaEffectCloud.getCustomEffects();
		}

		@Override
		public void addCustomEffect(PotionEffect effect, boolean overwrite) {
			areaEffectCloud.addCustomEffect(effect, overwrite);
		}

		@Override
		public void removeCustomEffect(PotionEffectType potionEffectType) {
			areaEffectCloud.removeCustomEffect(potionEffectType);
		}

		@Override
		public void clearCustomEffects() {
			areaEffectCloud.clearCustomEffects();
		}

	}

	private record ArrowAccessor(Arrow arrow) implements PotionAccessor {

		@Override
		public @Nullable PotionType getBasePotionType() {
			return arrow.getBasePotionType();
		}

		@Override
		public boolean hasCustomEffects() {
			return arrow.hasCustomEffects();
		}

		@Override
		public List<PotionEffect> getCustomEffects() {
			return arrow.getCustomEffects();
		}

		@Override
		public void addCustomEffect(PotionEffect effect, boolean overwrite) {
			arrow.addCustomEffect(effect, overwrite);
		}

		@Override
		public void removeCustomEffect(PotionEffectType potionEffectType) {
			arrow.removeCustomEffect(potionEffectType);
		}

		@Override
		public void clearCustomEffects() {
			arrow.clearCustomEffects();
		}

	}

}
