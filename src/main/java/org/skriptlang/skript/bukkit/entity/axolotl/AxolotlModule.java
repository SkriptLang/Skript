package org.skriptlang.skript.bukkit.entity.axolotl;

import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;

public class AxolotlModule extends HierarchicalAddonModule {

	public AxolotlModule(AddonModule parentModule) {
		super(parentModule);
	}

	@Override
	protected void loadSelf(SkriptAddon addon) {
		AxolotlData.register();

		register(addon,
			CondIsPlayingDead::register,
			EffPlayingDead::register
		);
	}

	@Override
	public String name() {
		return "axolotl";
	}

}
