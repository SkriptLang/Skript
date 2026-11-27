package org.skriptlang.skript.bukkit.world;

import ch.njol.skript.classes.EnumClassInfo;
import ch.njol.skript.registrations.Classes;
import io.papermc.paper.world.MoonPhase;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.bukkit.world.elements.effects.EffLoadWorld;
import org.skriptlang.skript.bukkit.world.elements.effects.EffSaveWorld;
import org.skriptlang.skript.bukkit.world.elements.events.*;
import org.skriptlang.skript.bukkit.world.elements.expressions.*;
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
		return List.of(new WorldBorderModule(this));
	}

	@Override
	protected void initSelf(SkriptAddon addon) {
		Classes.registerClass(new WorldClassInfo());
		Comparators.registerComparator(World.class, String.class, (world, name) -> Relation.get(world.getName().equalsIgnoreCase(name)));

		Classes.registerClass(new EnumClassInfo<>(World.Environment.class, "environment", "environments")
				.user("(world ?)?environments?")
				.name("World Environment")
				.description("Represents the environment of a <a href='#world'>world</a>.")
				.since("2.7"));

		Classes.registerClass(new EnumClassInfo<>(Difficulty.class, "difficulty", "difficulties")
				.user("difficult(y|ies)")
				.name("Difficulty")
				.description("The difficulty of a <a href='#world'>world</a>.")
				.since("2.3"));

		Classes.registerClass(new EnumClassInfo<>(MoonPhase.class, "moonphase", "moon phases")
			.user("(lunar|moon) ?phases?")
			.name("Moon Phase")
			.description("Represents the phase of a moon in a <a href='#world'>world</a>.")
			.since("2.7"));
	}

	@Override
	protected void loadSelf(SkriptAddon addon) {
		SyntaxRegistry syntaxRegistry = moduleRegistry(addon);
		EventValueRegistry eventValueRegistry = addon.registry(EventValueRegistry.class);

		WorldEvents.register(syntaxRegistry, eventValueRegistry);
		EvtWeatherChange.register(syntaxRegistry, eventValueRegistry);

		register(addon,
			EvtWorldInit::register,
			EvtWorldLoad::register,
			EvtWorldSave::register,
			EvtWorldUnload::register,
			EffLoadWorld::register,
			EffSaveWorld::register,
			ExprDifficulty::register,
			ExprGameRule::register,
			ExprMoonPhase::register,
			ExprSeaLevel::register,
			ExprWorld::register,
			ExprWorldEnvironment::register,
			ExprWorldFromName::register,
			ExprWorldSeed::register,
			ExprWorldSpawn::register
		);
	}

	@Override
	public String name() {
		return "world";
	}

}
