package org.skriptlang.skript.bukkit.pdc;

import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.pdc.expressions.ExprPersistentData;

public class PDCModule implements AddonModule {
	@Override
	public void load(SkriptAddon addon) {
		ExprPersistentData.register(addon.syntaxRegistry());
	}
}
