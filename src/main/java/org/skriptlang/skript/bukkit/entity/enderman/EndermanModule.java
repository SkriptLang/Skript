package org.skriptlang.skript.bukkit.entity.enderman;

import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;

public class EndermanModule extends HierarchicalAddonModule {

	public EndermanModule(AddonModule parentModule) {
		super(parentModule);
	}

	@Override
	protected void loadSelf(SkriptAddon addon) {
		EndermanData.register();
		
		register(addon,
			CondEndermanStaredAt::register,
			EffEndermanTeleport::register,
			ExprCarryingBlockData::register
		);
	}

	@Override
	public String name() {
		return "enderman";
	}

}
