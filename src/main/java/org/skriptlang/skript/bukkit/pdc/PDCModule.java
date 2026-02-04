package org.skriptlang.skript.bukkit.pdc;

import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.pdc.expressions.ExprPersistentData;
import org.skriptlang.skript.docs.Origin;

public class PDCModule implements AddonModule {

	@Override
	public void load(SkriptAddon addon) {
		Origin moduleOrigin = AddonModule.origin(addon, this);
		ExprPersistentData.register(addon.syntaxRegistry(), moduleOrigin);
	}

	@Override
	public String name() {
		return "persistent data containers";
	}

}
