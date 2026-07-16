package org.skriptlang.skript.bukkit.world;

import ch.njol.skript.registrations.Classes;
import org.bukkit.World;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.bukkit.world.elements.events.*;
import org.skriptlang.skript.bukkit.world.worldborder.elements.WorldBorderModule;
import org.skriptlang.skript.lang.comparator.Comparators;
import org.skriptlang.skript.lang.comparator.Relation;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.List;

public class WorldModule extends HierarchicalAddonModule {

	public WorldModule(AddonModule parentModule) {
		super(parentModule);
	}

	public Iterable<AddonModule> children() {
		return List.of(
			new WorldBorderModule(this)
		);
	}

	@Override
	protected void initSelf(SkriptAddon addon) {
		Classes.registerClass(new WorldClassInfo());
		Comparators.registerComparator(World.class, String.class, (world, name) -> Relation.get(world.getName().equalsIgnoreCase(name)));
	}

	@Override
	protected void loadSelf(SkriptAddon addon) {
		SyntaxRegistry syntaxRegistry = moduleRegistry(addon);
		EventValueRegistry eventValueRegistry = addon.registry(EventValueRegistry.class);

		WorldEvents.register(syntaxRegistry, eventValueRegistry);
		register(addon,
			EvtWeatherChange::register,
			EvtWorldInit::register,
			EvtWorldSave::register,
			EvtWorldUnload::register
		);
	}

	@Override
	public String name() {
		return "world";
	}

}
