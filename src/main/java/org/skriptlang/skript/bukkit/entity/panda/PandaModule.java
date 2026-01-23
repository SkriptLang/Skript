package org.skriptlang.skript.bukkit.entity.panda;

import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class PandaModule implements AddonModule {

	@Override
	public void load(SkriptAddon addon) {
		PandaData.register();

		SyntaxRegistry registry = addon.syntaxRegistry();
		CondPandaIsOnBack.register(registry);
		CondPandaIsRolling.register(registry);
		CondPandaIsScared.register(registry);
		CondPandaIsSneezing.register(registry);
		EffPandaOnBack.register(registry);
		EffPandaRolling.register(registry);
		EffPandaSneezing.register(registry);
	}

	@Override
	public String name() {
		return "panda";
	}

}
