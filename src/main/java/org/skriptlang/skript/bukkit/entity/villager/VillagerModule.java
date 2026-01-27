package org.skriptlang.skript.bukkit.entity.villager;

import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class VillagerModule implements AddonModule {

	@Override
	public void load(SkriptAddon addon) {
		VillagerData.register();
		ZombieVillagerData.register();

		SyntaxRegistry registry = addon.syntaxRegistry();
		ExprVillagerLevel.register(registry);
		ExprVillagerProfession.register(registry);
		ExprVillagerType.register(registry);
	}

	@Override
	public String name() {
		return "villager";
	}

}
