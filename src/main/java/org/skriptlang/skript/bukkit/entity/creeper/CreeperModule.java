package org.skriptlang.skript.bukkit.entity.creeper;

import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class CreeperModule implements AddonModule {

	@Override
	public void load(SkriptAddon addon) {
		CreeperData.register();

		SyntaxRegistry registry = addon.syntaxRegistry();
		EffExplodeCreeper.register(registry);
	}

	@Override
	public String name() {
		return "creeper";
	}

}
