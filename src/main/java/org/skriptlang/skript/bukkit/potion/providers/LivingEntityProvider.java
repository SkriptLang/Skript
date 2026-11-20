package org.skriptlang.skript.bukkit.potion.providers;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.util.Timespan;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.skriptlang.skript.bukkit.potion.elements.expressions.ExprPotionDuration;
import org.skriptlang.skript.bukkit.potion.util.PotionUtils;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;

class LivingEntityProvider extends PotionEffectProvider<LivingEntity> {

	public LivingEntityProvider(LivingEntity source) {
		super(source);
	}

	@Override
	public Collection<SkriptPotionEffect> get(PotionEffectType[] potionEffectTypes, RetrievalState state) {
		List<SkriptPotionEffect> potionEffects = new ArrayList<>();
		for (PotionEffectType type : potionEffectTypes) {
			PotionEffect potionEffect = source.getPotionEffect(type);
			if (potionEffect != null) {
				getEffects(potionEffect, state, potionEffects);
			}
		}
		return potionEffects;
	}

	@Override
	public Collection<SkriptPotionEffect> getAll(RetrievalState state) {
		List<SkriptPotionEffect> potionEffects = new ArrayList<>();
		for (PotionEffect potionEffect : source.getActivePotionEffects()) {
			getEffects(potionEffect, state, potionEffects);
		}
		return potionEffects;
	}

	private void getEffects(PotionEffect potionEffect, RetrievalState state, Collection<SkriptPotionEffect> destination) {
		if (state.includesActive()) {
			destination.add(SkriptPotionEffect.fromBukkitEffect(potionEffect, this));
		}
		if (state.includesHidden()) {
			PotionEffect hiddenEffect = potionEffect.getHiddenPotionEffect();
			while (hiddenEffect != null) {
				// do not set source for hidden effects
				destination.add(SkriptPotionEffect.fromBukkitEffect(hiddenEffect));
				hiddenEffect = hiddenEffect.getHiddenPotionEffect();
			}
		}
	}

	@Override
	public void add(PotionEffect potionEffect) {
		source.addPotionEffect(potionEffect);
	}

	@Override
	public void remove(SkriptPotionEffect potionEffect, RetrievalState state) {
		PotionEffect entityEffect = source.getPotionEffect(potionEffect.potionEffectType());
		if (entityEffect == null) {
			return;
		}

		Deque<PotionEffect> effects = PotionUtils.getHiddenEffects(entityEffect);
		boolean madeChanges = false;

		// retain (some or all) hidden effects
		// we only remove hidden effects if the user explicitly included them
		if (state.includesHidden()) {
			var effectsIterator = effects.iterator();
			while (effectsIterator.hasNext()) {
				if (potionEffect.matchesQualities(effectsIterator.next())) {
					effectsIterator.remove();
					madeChanges = true;
				}
			}
		}

		// retain the active effect
		// unless the user is only removing hidden effects, we attempt to filter the active effect
		if (state == RetrievalState.HIDDEN || !potionEffect.matchesQualities(entityEffect)) { // preserve the effect
			effects.addLast(entityEffect);
		} else {
			madeChanges = true;
		}

		if (madeChanges) { // only remove and apply if changes were made
			source.removePotionEffect(entityEffect.getType());
			source.addPotionEffects(effects);
		}
	}

	@Override
	public void removeAll(PotionEffectType potionEffectType,  RetrievalState state) {
		if (state == RetrievalState.ACTIVE || state == RetrievalState.HIDDEN) {
			clear(new PotionEffectType[]{potionEffectType}, state);
		} else {
			source.removePotionEffect(potionEffectType);
		}
	}

	@Override
	public void clear(PotionEffectType[] potionEffectTypes, RetrievalState state) {
		if (state == RetrievalState.ACTIVE) { // preserve hidden effects
			for (PotionEffectType type : potionEffectTypes) {
				PotionEffect potionEffect = source.getPotionEffect(type);
				if (potionEffect == null) {
					continue;
				}
				Deque<PotionEffect> hiddenEffects = PotionUtils.getHiddenEffects(potionEffect);
				source.removePotionEffect(type);
				source.addPotionEffects(hiddenEffects);
			}
		} else if (state == RetrievalState.HIDDEN) { // preserve active effect
			for (PotionEffectType type : potionEffectTypes) {
				PotionEffect original = source.getPotionEffect(type);
				source.removePotionEffect(type);
				if (original != null) {
					// applying a potion effect ignores the hidden effect value
					source.addPotionEffect(original);
				}
			}
		} else {
			for (PotionEffectType type : potionEffectTypes) {
				source.removePotionEffect(type);
			}
		}
	}

	@Override
	public void clearAll(RetrievalState state) {
		if (state == RetrievalState.ACTIVE) { // preserve hidden effects
			for (PotionEffect potionEffect : source.getActivePotionEffects()) {
				Deque<PotionEffect> hiddenEffects = PotionUtils.getHiddenEffects(potionEffect);
				source.removePotionEffect(potionEffect.getType());
				source.addPotionEffects(hiddenEffects);
			}
		} else if (state == RetrievalState.HIDDEN) { // preserve active effect
			for (PotionEffect potionEffect : source.getActivePotionEffects()) {
				source.removePotionEffect(potionEffect.getType());
				// applying a potion effect ignores the hidden effect value
				source.addPotionEffect(potionEffect);
			}
		} else {
			source.clearActivePotionEffects();
		}
	}

	@Override
	public void modify(PotionEffectType[] types, RetrievalState state, Object[] delta, ChangeMode mode) {
		for (PotionEffectType type : types) {
			PotionEffect potionEffect = source.getPotionEffect(type);
			if (potionEffect == null) {
				continue;
			}

			Deque<PotionEffect> finalEffects; // effects to be applied
			Deque<PotionEffect> effects; // effects to be filtered
			boolean madeChanges = false;

			if (state.includesHidden()) { // modify hidden effects
				finalEffects = new ArrayDeque<>();
				effects = PotionUtils.getHiddenEffects(potionEffect);
			} else { // otherwise, simply preserve the hidden effects
				finalEffects = PotionUtils.getHiddenEffects(potionEffect);
				effects = new ArrayDeque<>();
			}

			if (state.includesActive()) { // need to modify the active effect too
				effects.addLast(potionEffect);
			}

			// filter effects
			effectLoop: for (PotionEffect effect : effects) {
				SkriptPotionEffect skriptEffect = SkriptPotionEffect.fromBukkitEffect(effect);
				for (Object object : delta) {
					if (object instanceof Timespan timespan) {
						ExprPotionDuration.changeSafe(skriptEffect, timespan, mode);
						madeChanges = true;
					} else if (object instanceof SkriptPotionEffect base) {
						if (base.matchesQualities(effect)) { // remove this effect
							madeChanges = true;
							continue effectLoop;
						}
					}
				}
				// since we iterate most to least hidden, we need to preserve that order
				finalEffects.addLast(skriptEffect.asBukkitPotionEffect());
			}
			if (!madeChanges) { // no potion effects were modified, don't reapply effects
				return;
			}

			if (!state.includesActive()) { // if we didn't modify the active effect, we need to push it now
				effects.addLast(potionEffect);
			}

			source.removePotionEffect(type);
			source.addPotionEffects(finalEffects);
		}
	}

	@Override
	public void mirrorEffectChanges(SkriptPotionEffect potionEffect, Runnable runnable) {
		Deque<PotionEffect> hiddenEffects = null;
		PotionEffectType potionEffectType = potionEffect.potionEffectType();
		if (source.hasPotionEffect(potionEffectType)) {
			//noinspection DataFlowIssue - NotNull by hasPotionEffect check
			hiddenEffects = PotionUtils.getHiddenEffects(source.getPotionEffect(potionEffectType));
			source.removePotionEffect(potionEffectType);
		}
		runnable.run();
		PotionEffect updatedPotionEffect = potionEffect.asBukkitPotionEffect();
		if (hiddenEffects != null) { // reapply hidden effects
			for (PotionEffect hiddenEffect : hiddenEffects) {
				// we need to add this potion effect in the right order
				// it might end up not being applied at all, but we'll let the game determine that
				if (updatedPotionEffect != null &&
					(hiddenEffect.isShorterThan(updatedPotionEffect) || hiddenEffect.getAmplifier() > updatedPotionEffect.getAmplifier())) {
					source.addPotionEffect(updatedPotionEffect);
					updatedPotionEffect = null;
				}
				source.addPotionEffect(hiddenEffect);
			}
		}
		if (updatedPotionEffect != null) {
			source.addPotionEffect(updatedPotionEffect);
		}
	}

}
