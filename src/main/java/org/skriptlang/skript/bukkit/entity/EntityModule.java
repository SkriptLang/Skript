package org.skriptlang.skript.bukkit.entity;

import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.entity.coppergolem.CopperGolemModule;

public class EntityModule implements AddonModule {

	@Override
	public void load(SkriptAddon addon) {
		addon.loadModules(new CopperGolemModule());
	}

}
