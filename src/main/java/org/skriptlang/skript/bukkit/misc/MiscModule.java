package org.skriptlang.skript.bukkit.misc;

import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.misc.elements.expressions.ExprWithYawPitch;

import java.util.List;

public class MiscModule extends HierarchicalAddonModule {

	public MiscModule(AddonModule parentModule) {
		super(parentModule);
	}

	@Override
	protected void loadSelf(SkriptAddon addon) {
		register(addon, List.of(
			ExprWithYawPitch::register
		));
	}

	@Override
	public String name() {
		return "bukkit/misc";
	}

}
