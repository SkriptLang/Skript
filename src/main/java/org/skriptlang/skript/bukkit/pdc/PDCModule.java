package org.skriptlang.skript.bukkit.pdc;

import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.pdc.expressions.ExprPersistentData;

public class PDCModule extends HierarchicalAddonModule {

	public PDCModule(@Nullable AddonModule parentModule) {
		super(parentModule);
	}

	@Override
	protected void loadSelf(SkriptAddon addon) {
		register(addon, ExprPersistentData::register);
	}

	@Override
	public String name() {
		return "persistent data containers";
	}

}
