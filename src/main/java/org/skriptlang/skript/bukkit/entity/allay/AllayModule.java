package org.skriptlang.skript.bukkit.entity.allay;

import org.bukkit.entity.Allay;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.entity.data.SimpleEntityData;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class AllayModule implements AddonModule {

	@Override
	public void load(SkriptAddon addon) {
		SimpleEntityData.addSimpleEntity(Allay.class, "allay¦s @an", "allay[plural:s]");

		SyntaxRegistry registry = addon.syntaxRegistry();
		CondAllayCanDuplicate.register(registry);
		EffAllayCanDuplicate.register(registry);
		EffAllayDuplicate.register(registry);
		ExprAllayJukebox.register(registry);
	}

	@Override
	public String name() {
		return "allay";
	}

}
