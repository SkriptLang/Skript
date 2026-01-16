package org.skriptlang.skript.bukkit.entity.goat;

import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class GoatModule implements AddonModule {

	@Override
	public void load(SkriptAddon addon) {
		GoatData.register();

		SyntaxRegistry registry = addon.syntaxRegistry();

		CondGoatHasHorns.register(registry);
		EffGoatHorns.register(registry);
		EffGoatRam.register(registry);
	}

	@Override
	public String name() {
		return "goat";
	}

}
