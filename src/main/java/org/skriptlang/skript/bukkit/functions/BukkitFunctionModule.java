package org.skriptlang.skript.bukkit.functions;

import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;

public class BukkitFunctionModule extends HierarchicalAddonModule {

	public BukkitFunctionModule(AddonModule parentModule) {
		super(parentModule);
	}

	@Override
	protected void loadSelf(SkriptAddon addon) {
		new BukkitFunctions();
		new VectorFunctions();
	}

	@Override
	public String name() {
		return "functions";
	}
}
