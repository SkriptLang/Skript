package org.skriptlang.skript.bukkit.entity.minecart;

import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class MinecartModule implements AddonModule {

	@Override
	public void load(SkriptAddon addon) {
		MinecartData.register();

		SyntaxRegistry registry = addon.syntaxRegistry();
		ExprMaxMinecartSpeed.register(registry);
		ExprMinecartDerailedFlyingVelocity.register(registry);
	}

	@Override
	public String name() {
		return "minecart";
	}

}
