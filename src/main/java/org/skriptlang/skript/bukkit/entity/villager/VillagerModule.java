package org.skriptlang.skript.bukkit.entity.villager;

import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;

public class VillagerModule extends HierarchicalAddonModule {

	public VillagerModule(AddonModule parentModule) {
		super(parentModule);
	}

	@Override
	protected void loadSelf(SkriptAddon addon) {
		VillagerData.register();
		ZombieVillagerData.register();
		
		register(addon,
			ExprVillagerLevel::register,
			ExprVillagerProfession::register,
			ExprVillagerType::register
		);
	}

	@Override
	public String name() {
		return "villager";
	}

}
