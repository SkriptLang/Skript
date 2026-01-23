package org.skriptlang.skript.bukkit.entity.strider;

import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class StriderModule implements AddonModule {

	@Override
	public void load(SkriptAddon addon) {
		StriderData.register();

		SyntaxRegistry registry = addon.syntaxRegistry();
		EffStriderShivering.register(registry);
	}

	@Override
	public String name() {
		return "strider";
	}

}
