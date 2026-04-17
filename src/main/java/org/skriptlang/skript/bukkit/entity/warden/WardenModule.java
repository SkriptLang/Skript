package org.skriptlang.skript.bukkit.entity.warden;

import org.bukkit.entity.Warden;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.entity.data.SimpleEntityData;

public class WardenModule extends HierarchicalAddonModule {

	public WardenModule(AddonModule parentModule) {
		super(parentModule);
	}

	@Override
	protected void loadSelf(SkriptAddon addon) {
		SimpleEntityData.addSimpleEntity(Warden.class, "warden¦s @a", "warden[plural:s]");
		
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
