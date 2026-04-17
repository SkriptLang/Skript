package org.skriptlang.skript.bukkit.entity.minecart;

import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;

public class MinecartModule extends HierarchicalAddonModule {

	public MinecartModule(AddonModule parentModule) {
		super(parentModule);
	}

	@Override
	protected void loadSelf(SkriptAddon addon) {
		MinecartData.register();
		
		register(addon,
			ExprMaxMinecartSpeed::register,
			ExprMinecartDerailedFlyingVelocity::register
		);
	}

	@Override
	public String name() {
		return "minecart";
	}

}
