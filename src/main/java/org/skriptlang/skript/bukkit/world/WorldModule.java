package org.skriptlang.skript.bukkit.world;

import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.world.worldborder.elements.WorldBorderModule;

import java.util.List;

public class WorldModule extends HierarchicalAddonModule {

	public WorldModule(AddonModule parentModule) {
		super(parentModule);
	}

	public Iterable<AddonModule> children() {
		return List.of(
			new WorldBorderModule(this)
		);
	}

	@Override
	protected void initSelf(SkriptAddon addon) {

	}

	@Override
	protected void loadSelf(SkriptAddon addon) {

	}

	@Override
	public String name() {
		return "world";
	}

}
