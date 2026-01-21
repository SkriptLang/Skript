package org.skriptlang.skript.bukkit.block;

import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.block.sign.*;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class BlockModule implements AddonModule {

	@Override
	public void load(SkriptAddon addon) {
		SyntaxRegistry syntaxRegistry = addon.syntaxRegistry();

		// sign
		ExprSignText.register(syntaxRegistry);
	}

	@Override
	public String name() {
		return "block";
	}

}
