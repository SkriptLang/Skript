package org.skriptlang.skript.bukkit.inventory;

import ch.njol.skript.registrations.Classes;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.inventory.elements.InventoryEvents;
import org.skriptlang.skript.bukkit.inventory.elements.events.EvtInventoryClick;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class InventoryModule extends HierarchicalAddonModule {

	public InventoryModule(AddonModule parentModule) {
		super(parentModule);
	}

	@Override
	protected void initSelf(SkriptAddon addon) {
		Classes.registerClass(new InventoryClassInfo());
	}

	@Override
	protected void loadSelf(SkriptAddon addon) {
		SyntaxRegistry syntaxRegistry = moduleRegistry(addon);
		EventValueRegistry eventValueRegistry = addon.registry(EventValueRegistry.class);

		InventoryEvents.register(syntaxRegistry, eventValueRegistry);
		EvtInventoryClick.register(syntaxRegistry, eventValueRegistry);
	}

	@Override
	public String name() {
		return "inventory";
	}

}
