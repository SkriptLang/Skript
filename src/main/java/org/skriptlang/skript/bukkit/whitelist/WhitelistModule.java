package org.skriptlang.skript.bukkit.whitelist;

import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.whitelist.elements.*;

public class WhitelistModule extends HierarchicalAddonModule {

	public WhitelistModule(AddonModule parentModule) {
		super(parentModule);
	}

	@Override
	protected void loadSelf(SkriptAddon addon) {
		register(addon,
			CondIsWhitelisted::register,
			CondWillBeWhitelisted::register,

			EffEnforceWhitelist::register,

			registry -> EvtPlayerWhitelist.register(addon, registry),
			EvtServerWhitelist::register,

			ExprWhitelist::register
		);
	}

	@Override
	public String name() {
		return "whitelist";
	}

}
