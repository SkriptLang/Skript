package org.skriptlang.skript.bukkit.entity.goat;

import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;

public class GoatModule extends HierarchicalAddonModule {

	public GoatModule(AddonModule parentModule) {
		super(parentModule);
	}

	@Override
	protected void loadSelf(SkriptAddon addon) {
		GoatData.register();
		
		register(addon,
			CondGoatHasHorns::register,
			EffGoatHorns::register,
			EffGoatRam::register
		);
	}

	@Override
	public String name() {
		return "goat";
	}

}
