package org.skriptlang.skript.bukkit.entity.camel;

import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;

public class CamelModule extends HierarchicalAddonModule {

	public CamelModule(AddonModule parentModule) {
		super(parentModule);
	}

	@Override
	protected void loadSelf(SkriptAddon addon) {
		register(addon, CondIsDashing::register);
	}

	@Override
	public String name() {
		return "camel";
	}

}
