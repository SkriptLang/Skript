package org.skriptlang.skript.bukkit.misc;

import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.misc.expressions.*;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class MiscModule implements AddonModule {

	@Override
	public void load(SkriptAddon addon) {
		SyntaxRegistry syntaxRegistry = addon.syntaxRegistry();

		// expressions
		ExprBroadcastMessage.register(syntaxRegistry);
		ExprMOTD.register(syntaxRegistry);
	}

	@Override
	public String name() {
		return "miscellaneous";
	}

}
