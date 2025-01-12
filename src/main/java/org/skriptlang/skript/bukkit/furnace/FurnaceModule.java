package org.skriptlang.skript.bukkit.furnace;

import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.furnace.elements.*;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class FurnaceModule implements AddonModule {

	@Override
	public void load(SkriptAddon addon) {
		SyntaxRegistry registry = addon.syntaxRegistry();

		EvtFurnace.register(registry);
		ExprFurnaceEventItems.register(registry);
		ExprFurnaceSlot.register(registry);
		ExprFurnaceTime.register(registry);
	}
}
