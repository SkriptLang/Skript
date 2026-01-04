package org.skriptlang.skript.bukkit;

import ch.njol.skript.Skript;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.brewing.BrewingModule;
import org.skriptlang.skript.bukkit.damagesource.DamageSourceModule;
import org.skriptlang.skript.bukkit.entity.EntityModule;
import org.skriptlang.skript.bukkit.fishing.FishingModule;
import org.skriptlang.skript.bukkit.itemcomponents.ItemComponentModule;
import org.skriptlang.skript.bukkit.potion.PotionModule;

import java.util.List;

public class BukkitModule implements AddonModule {

	private final List<AddonModule> allSubmodules = List.of(
			new BrewingModule(this),
			new EntityModule(this),
			new DamageSourceModule(this),
			new ItemComponentModule(this),
			new PotionModule(this),
			new FishingModule(this)

	);

	private List<AddonModule> filteredSubmodules;

	@Override
	public boolean canLoad(SkriptAddon addon) {
		if (!Skript.classExists("org.bukkit.Bukkit"))
			return false;
		filteredSubmodules = allSubmodules.stream()
				.filter(module -> module.canLoad(addon))
				.toList();
		return !filteredSubmodules.isEmpty();
	}

	@Override
	public void init(SkriptAddon addon) {
		// initialize submodules
		for (AddonModule module : filteredSubmodules) {
			module.init(addon);
		}
	}

	@Override
	public void load(SkriptAddon addon) {
		// load submodules
		for (AddonModule module : filteredSubmodules) {
			module.load(addon);
		}
	}

	@Override
	public String name() {
		return "bukkit";
	}

}
