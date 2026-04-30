package org.skriptlang.skript.bukkit.inventory;

import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.inventory.elements.events.EvtInventory;

public class InventoryModule extends HierarchicalAddonModule {

	public InventoryModule(AddonModule parentModule) { super(parentModule); }

	@Override
	public void loadSelf(SkriptAddon addon) {
		register(addon,
			EvtInventory::register
		);
	}

	@Override
	public String name() { return "inventory"; }

}
