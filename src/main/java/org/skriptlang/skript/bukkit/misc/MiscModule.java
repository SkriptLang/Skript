package org.skriptlang.skript.bukkit.misc;

import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.misc.expressions.ExprWithYawPitch;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class MiscModule implements AddonModule {

	@Override
	public void load(SkriptAddon addon) {
		SyntaxRegistry registry = addon.syntaxRegistry();
		ExprWithYawPitch.register(registry);
	}

	@Override
	public String name() {
		return "bukkit/misc";
	}

}
