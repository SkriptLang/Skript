package org.skriptlang.skript.bukkit.test;

import ch.njol.skript.test.runner.SkriptJUnitTest;
import org.skriptlang.skript.test.SkriptTestEvent;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.bukkit.test.elements.expressions.*;

public class BukkitTestModule implements AddonModule {

	@Override
	public void load(SkriptAddon addon) {
		register(addon,
			ExprTestBlock::register,
			ExprTestLocation::register,
			ExprTestOfflinePlayer::register,
			ExprTestWorld::register
		);

		EventValueRegistry eventValueRegistry = addon.registry(EventValueRegistry.class);
		eventValueRegistry.register(EventValue.simple(SkriptTestEvent.class, Block.class,
			ignored -> SkriptJUnitTest.getBlock()));
		eventValueRegistry.register(EventValue.simple(SkriptTestEvent.class, Location.class,
			ignored -> SkriptJUnitTest.getTestLocation()));
		eventValueRegistry.register(EventValue.simple(SkriptTestEvent.class, World.class,
			ignored -> SkriptJUnitTest.getTestWorld()));
	}

	@Override
	public String name() {
		return "bukkit testing";
	}

}
