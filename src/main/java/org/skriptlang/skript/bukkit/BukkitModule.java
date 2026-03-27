package org.skriptlang.skript.bukkit;

import ch.njol.skript.Skript;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.breeding.BreedingModule;
import org.skriptlang.skript.bukkit.brewing.BrewingModule;
import org.skriptlang.skript.bukkit.damagesource.DamageSourceModule;
import org.skriptlang.skript.bukkit.entity.EntityModule;
import org.skriptlang.skript.bukkit.fishing.FishingModule;
import org.skriptlang.skript.bukkit.block.furnace.FurnaceModule;
import org.skriptlang.skript.bukkit.input.InputModule;
import org.skriptlang.skript.bukkit.itemcomponents.ItemComponentModule;
import org.skriptlang.skript.bukkit.loottables.LootTableModule;
import org.skriptlang.skript.bukkit.misc.MiscModule;
import org.skriptlang.skript.bukkit.particles.ParticleModule;
import org.skriptlang.skript.bukkit.potion.PotionModule;
import org.skriptlang.skript.bukkit.tags.TagModule;

import java.util.List;

public class BukkitModule extends HierarchicalAddonModule {

	@Override
	protected boolean canLoadSelf(SkriptAddon addon) {
		return Skript.classExists("org.bukkit.Bukkit");
	}

	@Override
	public Iterable<AddonModule> children() {
		return List.of(
			new BreedingModule(this),
			new BrewingModule(this),
			new DamageSourceModule(this),
			new EntityModule(this),
			new FishingModule(this),
			new FurnaceModule(this),
			new InputModule(this),
			new ItemComponentModule(this),
			new LootTableModule(this),
			new MiscModule(this),
			new ParticleModule(this),
			new PotionModule(this),
			new TagModule(this)
		);
	}

	@Override
	protected void loadSelf(SkriptAddon addon) {
		// nothing to do
	}

	@Override
	public String name() {
		return "bukkit";
	}

}
