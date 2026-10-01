package org.skriptlang.skript.bukkit.item;

import ch.njol.skript.classes.EnumClassInfo;
import ch.njol.skript.registrations.Classes;
import org.bukkit.inventory.ItemRarity;
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
	public void initSelf(SkriptAddon addon) {
		Classes.registerClass(new EnumClassInfo<>(ItemRarity.class, "itemrarity", "item rarity")
			.user("item ?rarit(y|ies)")
			.name("Item Rarity")
			.description("Represents the item rarity of an item.")
			.since("INSERT VERSION")
		);
	}

	@Override
	public void loadSelf(SkriptAddon addon) {
		register(addon,
			ExprItemModel::register,
			ExprItemRarity::register,
			ExprItemWithLore::register,
			ExprItemWithModel::register,
			ExprLore::register
		);
	}

	@Override
	public String name() {
		return "item";
	}

}
