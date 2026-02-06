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
import org.skriptlang.skript.bukkit.furnace.FurnaceModule;
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
			new BrewingModule(this),
			new EntityModule(this),
			new DamageSourceModule(this),
			new ItemComponentModule(this),
			new PotionModule(this),
			new FishingModule(this),
			new ParticleModule(this),
			new MiscModule(this),
			new LootTableModule(this),
			new BreedingModule(this),
			new FurnaceModule(this),
			new InputModule(this),
			new TagModule(this)
		);
	}

	@Override
	public String name() {
		return "bukkit";
	}

}
