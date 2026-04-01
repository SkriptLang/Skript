package org.skriptlang.skript.bukkit;

import ch.njol.skript.Skript;
import ch.njol.skript.registrations.Classes;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.block.BlockModule;
import org.skriptlang.skript.bukkit.breeding.BreedingModule;
import org.skriptlang.skript.bukkit.brewing.BrewingModule;
import org.skriptlang.skript.bukkit.damagesource.DamageSourceModule;
import org.skriptlang.skript.bukkit.entity.EntityModule;
import org.skriptlang.skript.bukkit.fishing.FishingModule;
import org.skriptlang.skript.bukkit.input.InputModule;
import org.skriptlang.skript.bukkit.item.ItemModule;
import org.skriptlang.skript.bukkit.itemcomponents.ItemComponentModule;
import org.skriptlang.skript.bukkit.loottables.LootTableModule;
import org.skriptlang.skript.bukkit.misc.MiscModule;
import org.skriptlang.skript.bukkit.particles.ParticleModule;
import org.skriptlang.skript.bukkit.pdc.PDCModule;
import org.skriptlang.skript.bukkit.potion.PotionModule;
import org.skriptlang.skript.bukkit.tags.TagModule;
import org.skriptlang.skript.bukkit.text.TextModule;
import org.skriptlang.skript.bukkit.types.*;

import java.util.List;

public class BukkitModule extends HierarchicalAddonModule {

	@Override
	protected boolean canLoadSelf(SkriptAddon addon) {
		return Skript.classExists("org.bukkit.Bukkit");
	}

	@Override
	public Iterable<AddonModule> children() {
		return List.of(
			new BlockModule(this),
			new BreedingModule(this),
			new BrewingModule(this),
			new DamageSourceModule(this),
			new EntityModule(this),
			new FishingModule(this),
			new InputModule(this),
			new ItemModule(this),
			new ItemComponentModule(this),
			new LootTableModule(this),
			new MiscModule(this),
			new ParticleModule(this),
			new PDCModule(this),
			new PotionModule(this),
			new TagModule(this),
			new TextModule(this)
		);
	}

	@Override
	protected void initSelf(SkriptAddon addon) {
		Classes.registerClass(new BlockClassInfo());
		Classes.registerClass(new EntityClassInfo());
		Classes.registerClass(new InventoryClassInfo());
		Classes.registerClass(new ItemStackClassInfo());
		Classes.registerClass(new ItemTypeClassInfo());
		Classes.registerClass(new LocationClassInfo());
		Classes.registerClass(new NameableClassInfo());
		Classes.registerClass(new OfflinePlayerClassInfo());
		Classes.registerClass(new PlayerClassInfo());
		Classes.registerClass(new SlotClassInfo());
		Classes.registerClass(new VectorClassInfo());
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
