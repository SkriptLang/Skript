package org.skriptlang.skript.bukkit.entity.projectile;

import org.bukkit.entity.Projectile;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.entity.data.SimpleEntityData;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class ProjectileModule implements AddonModule {

	@Override
	public void load(SkriptAddon addon) {
		SimpleEntityData.addSuperEntity(Projectile.class, "projectile¦s @a", "projectile[plural:s]");

		SyntaxRegistry registry = addon.syntaxRegistry();
		ExprProjectileCriticalState.register(registry);
	}

	@Override
	public String name() {
		return "projectile";
	}

}
