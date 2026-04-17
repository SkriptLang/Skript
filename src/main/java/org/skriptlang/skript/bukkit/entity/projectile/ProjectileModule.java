package org.skriptlang.skript.bukkit.entity.projectile;

import org.bukkit.entity.Projectile;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.entity.data.SimpleEntityData;

public class ProjectileModule extends HierarchicalAddonModule {

	public ProjectileModule(AddonModule parentModule) {
		super(parentModule);
	}

	@Override
	protected void loadSelf(SkriptAddon addon) {
		SimpleEntityData.addSuperEntity(Projectile.class, "projectile¦s @a", "projectile[plural:s]");

		register(addon, ExprProjectileCriticalState::register);
	}

	@Override
	public String name() {
		return "projectile";
	}

}
