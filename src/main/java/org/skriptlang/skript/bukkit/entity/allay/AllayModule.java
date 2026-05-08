package org.skriptlang.skript.bukkit.entity.allay;

import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;

public class AllayModule extends HierarchicalAddonModule {

	public AllayModule(AddonModule parentModule) {
		super(parentModule);
	}

	@Override
	protected void loadSelf(SkriptAddon addon) {
		register(addon,
			CondAllayCanDuplicate::register,
			EffAllayCanDuplicate::register,
			EffAllayDuplicate::register,
			ExprAllayJukebox::register
		);
	}

	@Override
	public String name() {
		return "allay";
	}

}
