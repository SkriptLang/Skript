package org.skriptlang.skript.bukkit.entity.item;

import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class ItemModule implements AddonModule {

	@Override
	public void load(SkriptAddon addon) {
		DroppedItemData.register();

		SyntaxRegistry registry = addon.syntaxRegistry();
		ExprItemOwner.register(registry);
		ExprItemThrower.register(registry);
	}

	@Override
	public String name() {
		return "item";
	}

}
