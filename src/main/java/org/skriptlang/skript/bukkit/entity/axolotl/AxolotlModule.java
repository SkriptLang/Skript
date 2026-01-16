package org.skriptlang.skript.bukkit.entity.axolotl;

import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class AxolotlModule implements AddonModule {

	@Override
	public void load(SkriptAddon addon) {
		AxolotlData.register();

		SyntaxRegistry registry = addon.syntaxRegistry();

		CondIsPlayingDead.register(registry);
		EffPlayingDead.register(registry);
	}

	@Override
	public String name() {
		return "axolotl";
	}

}
