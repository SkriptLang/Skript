package org.skriptlang.skript.bukkit.brewing;

import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.brewing.elements.CondBrewingConsume;
import org.skriptlang.skript.bukkit.brewing.elements.EffBrewingConsume;
import org.skriptlang.skript.bukkit.brewing.elements.EvtBrewingComplete;
import org.skriptlang.skript.bukkit.brewing.elements.EvtBrewingFuel;
import org.skriptlang.skript.bukkit.brewing.elements.EvtBrewingStart;
import org.skriptlang.skript.bukkit.brewing.elements.ExprBrewingFuelLevel;
import org.skriptlang.skript.bukkit.brewing.elements.ExprBrewingResults;
import org.skriptlang.skript.bukkit.brewing.elements.ExprBrewingSlot;
import org.skriptlang.skript.bukkit.brewing.elements.ExprBrewingTime;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.Arrays;
import java.util.function.Consumer;

public class BrewingModule implements AddonModule {

	@Override
	public void load(SkriptAddon addon) {
		register(addon.syntaxRegistry(),

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

	private void register(SyntaxRegistry registry, Consumer<SyntaxRegistry>... consumers) {
		Arrays.stream(consumers).forEach(consumer -> consumer.accept(registry));
	}

	@Override
	public String name() {
		return "brewing";
	}

}
