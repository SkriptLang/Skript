package org.skriptlang.skript.bukkit.test;

import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.test.elements.expressions.*;

public class BukkitTestModule implements AddonModule {

	@Override
	public void load(SkriptAddon addon) {
		register(addon,
			ExprTestBlock::register,
			ExprTestLocation::register,
			ExprTestOfflinePlayer::register,
			ExprTestWorld::register
		);
	}

	@Override
	public String name() {
		return "bukkit testing";
	}

}
