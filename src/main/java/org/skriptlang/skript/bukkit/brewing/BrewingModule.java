package org.skriptlang.skript.bukkit.brewing;

import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.brewing.elements.conditions.CondBrewingConsume;
import org.skriptlang.skript.bukkit.brewing.elements.effects.EffBrewingConsume;
import org.skriptlang.skript.bukkit.brewing.elements.events.EvtBrewingComplete;
import org.skriptlang.skript.bukkit.brewing.elements.events.EvtBrewingFuel;
import org.skriptlang.skript.bukkit.brewing.elements.events.EvtBrewingStart;
import org.skriptlang.skript.bukkit.brewing.elements.expressions.ExprBrewingFuelLevel;
import org.skriptlang.skript.bukkit.brewing.elements.expressions.ExprBrewingResults;
import org.skriptlang.skript.bukkit.brewing.elements.expressions.ExprBrewingSlot;
import org.skriptlang.skript.bukkit.brewing.elements.expressions.ExprBrewingTime;

/**
 * Module containing brewing stand related elements.
 */
public class BrewingModule extends HierarchicalAddonModule {

	public BrewingModule(AddonModule parentModule) {
		super(parentModule);
	}

	@Override
	protected void loadSelf(SkriptAddon addon) {
		register(addon,
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
