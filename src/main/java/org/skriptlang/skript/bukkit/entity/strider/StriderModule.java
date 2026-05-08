package org.skriptlang.skript.bukkit.entity.strider;

import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;

public class StriderModule extends HierarchicalAddonModule {

	public StriderModule(AddonModule parentModule) {
		super(parentModule);
	}

	@Override
	protected void loadSelf(SkriptAddon addon) {
		StriderData.register();
		
		register(addon,
			EffStriderShivering::register,
			CondStriderIsShivering::register
		);
	}

	@Override
	public String name() {
		return "strider";
	}

}
