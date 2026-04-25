package org.skriptlang.skript.bukkit.block.sign;

import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.block.sign.elements.expressions.ExprSignText;

public class SignModule extends HierarchicalAddonModule {

	public SignModule(AddonModule parentModule) {
		super(parentModule);
	}

	@Override
	protected void loadSelf(SkriptAddon addon) {
		register(addon,
			ExprSignText::register
		);
	}

	@Override
	public String name() {
		return "sign";
	}

}
