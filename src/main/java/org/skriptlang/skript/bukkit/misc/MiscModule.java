package org.skriptlang.skript.bukkit.misc;

import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.ChildAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.misc.elements.expressions.ExprWithYawPitch;

import java.util.List;

public class MiscModule extends ChildAddonModule {

	/**
	 * Constructs a child addon module with the given parent module.
	 *
	 * @param parentModule The parent module that created this child module.
	 */
	public MiscModule(AddonModule parentModule) {
		super(parentModule);
	}

	@Override
	public void load(SkriptAddon addon) {
		register(addon, List.of(
			ExprWithYawPitch::register
		));
	}

	@Override
	public String name() {
		return "bukkit/misc";
	}

}
