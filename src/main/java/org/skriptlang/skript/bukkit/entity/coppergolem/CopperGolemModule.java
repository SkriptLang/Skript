package org.skriptlang.skript.bukkit.entity.coppergolem;

import ch.njol.skript.Skript;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;

public class CopperGolemModule implements AddonModule {

	@Override
	public boolean canLoad(SkriptAddon addon) {
		return Skript.classExists("org.bukkit.entity.CopperGolem");
	}

	@Override
	public void load(SkriptAddon addon) {
		ExprCopperGolemOxidationTime.register(addon.syntaxRegistry());

		CopperGolemData.register();
	}

}
