package org.skriptlang.skript.bukkit.entity.projectile;

import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;

public class ProjectileModule extends HierarchicalAddonModule {

	public ProjectileModule(AddonModule parentModule) {
		super(parentModule);
	}

	@Override
	protected void loadSelf(SkriptAddon addon) {
		register(addon, ExprProjectileCriticalState::register);
	}

	@Override
	public String name() {
		return "projectile";
	}

}
