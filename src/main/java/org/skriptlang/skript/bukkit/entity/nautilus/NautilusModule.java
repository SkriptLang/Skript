package org.skriptlang.skript.bukkit.entity.nautilus;

import ch.njol.skript.Skript;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;

public class NautilusModule extends HierarchicalAddonModule {

	public NautilusModule(AddonModule parentModule) {
		super(parentModule);
	}

	@Override
	protected boolean canLoadSelf(SkriptAddon addon) {
		return Skript.classExists("org.bukkit.entity.Nautilus");
	}

	@Override
	protected void loadSelf(SkriptAddon addon) {
		NautilusData.register();
		ZombieNautilusData.register();
	}

	@Override
	public String name() {
		return "nautilus";
	}

}
