package org.skriptlang.skript.bukkit.brewing;

import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.ChildAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.brewing.elements.*;

/**
 * Module containing brewing stand related elements.
 */
public class BrewingModule extends ChildAddonModule {

	public BrewingModule(AddonModule parentModule) {
		super(parentModule);
	}

	@Override
	public void load(SkriptAddon addon) {
		AddonModule.register(addon.syntaxRegistry(), origin(addon),
			CondBrewingConsume::register,

			EffBrewingConsume::register,

			EvtBrewingComplete::register,
			EvtBrewingFuel::register,
			EvtBrewingStart::register,

			ExprBrewingFuelLevel::register,
			ExprBrewingResults::register,
			ExprBrewingSlot::register,
			ExprBrewingTime::register
		);
	}

	@Override
	public String name() {
		return "brewing";
	}

}
