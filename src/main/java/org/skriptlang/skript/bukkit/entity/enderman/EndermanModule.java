package org.skriptlang.skript.bukkit.entity.enderman;

import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EndermanModule implements AddonModule {

	@Override
	public void load(SkriptAddon addon) {
		EndermanData.register();

		SyntaxRegistry registry = addon.syntaxRegistry();
		CondEndermanStaredAt.register(registry);
		EffEndermanTeleport.register(registry);
		ExprCarryingBlockData.register(registry);
	}

	@Override
	public String name() {
		return "enderman";
	}

}
