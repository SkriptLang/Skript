package org.skriptlang.skript.bukkit.entity.warden;

import org.bukkit.entity.Warden;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.entity.data.SimpleEntityData;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class WardenModule implements AddonModule {

	@Override
	public void load(SkriptAddon addon) {
		SimpleEntityData.addSimpleEntity("warden", Warden.class);

		SyntaxRegistry registry = addon.syntaxRegistry();

		EffWardenDisturbance.register(registry);
	}

	@Override
	public String name() {
		return "warden";
	}

}
