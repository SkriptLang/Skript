package org.skriptlang.skript.bukkit.vehicle;

import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.vehicle.elements.events.EvtVehicle;

public class VehicleModule extends HierarchicalAddonModule {

	public VehicleModule(AddonModule parentModule) { super(parentModule); }

	@Override
	public void loadSelf(SkriptAddon addon) {
		register(addon,
			EvtVehicle::register
		);
	}

	@Override
	public String name() { return "vehicle"; }

}
