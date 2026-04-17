package org.skriptlang.skript.bukkit.entity.allay;

import org.bukkit.entity.Allay;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.entity.data.SimpleEntityData;

public class AllayModule extends HierarchicalAddonModule {

	public AllayModule(AddonModule parentModule) {
		super(parentModule);
	}

	@Override
	protected void loadSelf(SkriptAddon addon) {
		SimpleEntityData.addSimpleEntity(Allay.class, "allay¦s @an", "allay[plural:s]");


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
