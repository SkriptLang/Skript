package org.skriptlang.skript.bukkit.block;

import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.block.furnace.FurnaceModule;
import org.skriptlang.skript.bukkit.block.sign.SignModule;

import java.util.List;

public class BlockModule extends HierarchicalAddonModule {

	public BlockModule(AddonModule parentModule) {
		super(parentModule);
	}

	@Override
	public Iterable<AddonModule> children() {
		return List.of(
			new FurnaceModule(this),
			new SignModule(this)
		);
	}

	@Override
	public void loadSelf(SkriptAddon addon) {
		// intentionally left blank
	}

	@Override
	public String name() {
		return "block";
	}

}
