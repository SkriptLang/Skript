package org.skriptlang.skript.bukkit.entity.warden;

import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;

public class WardenModule extends HierarchicalAddonModule {

	public WardenModule(AddonModule parentModule) {
		super(parentModule);
	}

	@Override
	protected void loadSelf(SkriptAddon addon) {
		register(addon,
			EffWardenDisturbance::register,
			ExprWardenAngryAt::register,
			ExprWardenEntityAnger::register
		);
	}

	@Override
	public String name() {
		return "warden";
	}

}
