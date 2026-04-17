package org.skriptlang.skript.bukkit.entity.ghast;

import ch.njol.skript.Skript;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.HappyGhast;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.entity.data.SimpleEntityData;

public class GhastModule extends HierarchicalAddonModule {

	public GhastModule(AddonModule parentModule) {
		super(parentModule);
	}

	@Override
	protected void loadSelf(SkriptAddon addon) {
		SimpleEntityData.addSimpleEntity(Ghast.class, "ghast¦s @a", "ghast[plural:s]");
		if (Skript.classExists("org.bukkit.entity.HappyGhast")) {
			SimpleEntityData.addSimpleEntity(HappyGhast.class, "happy ghast¦s @a", "<age> happy ghast[plural:s]",
				"baby:[happy] ghastling[plural:s]");
		}

		register(addon, CondIsChargingFireball::register);
	}

	@Override
	public String name() {
		return "ghast";
	}

}
