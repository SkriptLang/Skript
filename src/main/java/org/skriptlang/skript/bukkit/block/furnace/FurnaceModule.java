package org.skriptlang.skript.bukkit.block.furnace;

import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.block.furnace.elements.events.EvtFurnace;
import org.skriptlang.skript.bukkit.block.furnace.elements.expressions.ExprFurnaceEventItems;
import org.skriptlang.skript.bukkit.block.furnace.elements.expressions.ExprFurnaceSlot;
import org.skriptlang.skript.bukkit.block.furnace.elements.expressions.ExprFurnaceTime;

public class FurnaceModule extends HierarchicalAddonModule {

	public FurnaceModule(AddonModule parentModule) {
		super(parentModule);
	}

	@Override
	protected void loadSelf(SkriptAddon addon) {
		register(addon,
			EvtFurnace::register,
			ExprFurnaceEventItems::register,
			ExprFurnaceSlot::register,
			ExprFurnaceTime::register
		);
	}

	@Override
	public String name() {
		return "furnace";
	}

}
