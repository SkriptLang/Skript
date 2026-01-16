package org.skriptlang.skript.bukkit.entity.camel;

import org.bukkit.entity.Camel;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.entity.data.SimpleEntityData;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class CamelModule implements AddonModule {

	@Override
	public void load(SkriptAddon addon) {
		SimpleEntityData.addSimpleEntity("camel", Camel.class);

		SyntaxRegistry registry = addon.syntaxRegistry();
		CondIsDashing.register(registry);
	}

	@Override
	public String name() {
		return "camel";
	}

}
