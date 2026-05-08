package org.skriptlang.skript.bukkit.entity.ghast;

import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;

public class GhastModule extends HierarchicalAddonModule {

	public GhastModule(AddonModule parentModule) {
		super(parentModule);
	}

	@Override
	protected void loadSelf(SkriptAddon addon) {
		register(addon, CondIsChargingFireball::register);
	}

	@Override
	public String name() {
		return "ghast";
	}

}
