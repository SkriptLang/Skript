package org.skriptlang.skript.bukkit.item;

import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.item.book.*;
import org.skriptlang.skript.bukkit.item.misc.*;

public class ItemModule extends HierarchicalAddonModule {

	public ItemModule(AddonModule parentModule) {
		super(parentModule);
	}

	@Override
	public void loadSelf(SkriptAddon addon) {
		register(addon,
			// book
			ExprBookAuthor::register,
			ExprBookPages::register,
			ExprBookTitle::register,
			// miscellaneous
			ExprItemWithLore::register,
			ExprLore::register
		);
	}

	@Override
	public String name() {
		return "item";
	}

}
