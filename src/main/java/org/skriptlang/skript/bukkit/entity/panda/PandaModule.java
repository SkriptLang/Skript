package org.skriptlang.skript.bukkit.entity.panda;

import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;

public class PandaModule extends HierarchicalAddonModule {

	public PandaModule(AddonModule parentModule) {
		super(parentModule);
	}

	@Override
	protected void loadSelf(SkriptAddon addon) {
		PandaData.register();
		
		register(addon,
			CondPandaIsOnBack::register,
			CondPandaIsRolling::register,
			CondPandaIsScared::register,
			CondPandaIsSneezing::register,
			EffPandaOnBack::register,
			EffPandaRolling::register,
			EffPandaSneezing::register,
			ExprPandaGene::register
		);
	}

	@Override
	public String name() {
		return "panda";
	}

}
