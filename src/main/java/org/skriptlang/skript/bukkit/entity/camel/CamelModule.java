package org.skriptlang.skript.bukkit.entity.camel;

import org.bukkit.entity.Camel;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.entity.data.SimpleEntityData;

public class CamelModule extends HierarchicalAddonModule {

	public CamelModule(AddonModule parentModule) {
		super(parentModule);
	}

	@Override
	protected void loadSelf(SkriptAddon addon) {
		SimpleEntityData.addSimpleEntity(Camel.class, "camel¦s @a", "camel[plural:s]");

		register(addon, CondIsDashing::register);
	}

	@Override
	public String name() {
		return "camel";
	}

}
