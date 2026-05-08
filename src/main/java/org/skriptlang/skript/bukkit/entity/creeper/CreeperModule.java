package org.skriptlang.skript.bukkit.entity.creeper;

import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;

public class CreeperModule extends HierarchicalAddonModule {

	public CreeperModule(AddonModule parentModule) {
		super(parentModule);
	}

	@Override
	protected void loadSelf(SkriptAddon addon) {
		CreeperData.register();

		register(addon, EffExplodeCreeper::register);
	}

	@Override
	public String name() {
		return "creeper";
	}

}
