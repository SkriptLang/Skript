package org.skriptlang.skript.bukkit.item;

import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.item.book.*;
import org.skriptlang.skript.bukkit.item.misc.*;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class ItemModule implements AddonModule {

	@Override
	public void load(SkriptAddon addon) {
		SyntaxRegistry syntaxRegistry = addon.syntaxRegistry();

		// book
		ExprBookAuthor.register(syntaxRegistry);
		ExprBookPages.register(syntaxRegistry);
		ExprBookTitle.register(syntaxRegistry);

		// misc
		ExprItemWithLore.register(syntaxRegistry);
		ExprLore.register(syntaxRegistry);
	}

	@Override
	public String name() {
		return "item";
	}

}
