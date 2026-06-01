package org.skriptlang.skript.bukkit.item;

import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.item.book.BookModule;
import org.skriptlang.skript.bukkit.item.elements.*;

import java.util.List;

public class ItemModule extends HierarchicalAddonModule {

	public ItemModule(AddonModule parentModule) {
		super(parentModule);
	}

	@Override
	public Iterable<AddonModule> children() {
		return List.of(
			new BookModule(this)
		);
	}

	@Override
	public void loadSelf(SkriptAddon addon) {
		register(addon,
			ExprItemWithLore::register,
			ExprLore::register
		);
	}

	@Override
	public String name() {
		return "item";
	}

}
