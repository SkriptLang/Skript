package org.skriptlang.skript.bukkit.item;

import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.item.elements.*;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class ItemModule implements AddonModule {

	@Override
	public void load(SkriptAddon addon) {
		SyntaxRegistry syntaxRegistry = addon.syntaxRegistry();
		ExprItemWithLore.register(syntaxRegistry);
		ExprLore.register(syntaxRegistry);
	}

}
