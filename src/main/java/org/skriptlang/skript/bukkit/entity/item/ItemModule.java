package org.skriptlang.skript.bukkit.entity.item;

import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;

public class ItemModule extends HierarchicalAddonModule {

	public ItemModule(AddonModule parentModule) {
		super(parentModule);
	}

	@Override
	protected void loadSelf(SkriptAddon addon) {
		DroppedItemData.register();
		
		register(addon,
			ExprItemOwner::register,
			ExprItemThrower::register
		);
	}

	@Override
	public String name() {
		return "item";
	}

}
