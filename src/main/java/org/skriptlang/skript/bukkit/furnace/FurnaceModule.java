package org.skriptlang.skript.bukkit.furnace;

import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.ChildAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.furnace.elements.EvtFurnace;
import org.skriptlang.skript.bukkit.furnace.elements.ExprFurnaceEventItems;
import org.skriptlang.skript.bukkit.furnace.elements.ExprFurnaceSlot;
import org.skriptlang.skript.bukkit.furnace.elements.ExprFurnaceTime;

import java.util.List;

public class FurnaceModule extends ChildAddonModule {

	public FurnaceModule(AddonModule parentModule) {
		super(parentModule);
	}

	@Override
	public void load(SkriptAddon addon) {
		register(addon, List.of(
			EvtFurnace::register,
			ExprFurnaceEventItems::register,
			ExprFurnaceSlot::register,
			ExprFurnaceTime::register
		));
	}

	@Override
	public String name() {
		return "furnace";
	}

}
