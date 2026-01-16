package org.skriptlang.skript.bukkit.entity.ghast;

import org.bukkit.entity.Ghast;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.entity.data.SimpleEntityData;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class GhastModule implements AddonModule {

	@Override
	public void load(SkriptAddon addon) {
		SimpleEntityData.addSimpleEntity("ghast", Ghast.class);

		SyntaxRegistry registry = addon.syntaxRegistry();

		CondIsChargingFireball.register(registry);
	}

	@Override
	public String name() {
		return "ghast";
	}

}
