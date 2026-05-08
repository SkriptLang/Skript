package org.skriptlang.skript.bukkit.potion.providers;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;

import java.util.Collection;
import java.util.List;

public class NullProvider extends PotionEffectProvider<Object> {

	public NullProvider() {
		super(null);
	}

	@Override
	public Collection<SkriptPotionEffect> get(PotionEffectType[] potionEffectTypes, RetrievalState state) {
		return List.of();
	}

	@Override
	public Collection<SkriptPotionEffect> getAll(RetrievalState state) {
		return List.of();
	}

	@Override
	public void add(PotionEffect potionEffect) {

	}

	@Override
	public void remove(SkriptPotionEffect potionEffect, RetrievalState state) {

	}

	@Override
	public void removeAll(PotionEffectType potionEffectType, RetrievalState state) {

	}

	@Override
	public void clear(PotionEffectType[] potionEffectTypes, RetrievalState state) {

	}

	@Override
	public void clearAll(RetrievalState state) {

	}

	@Override
	public void mirrorEffectChanges(SkriptPotionEffect potionEffect, Runnable runnable) {

	}

}
