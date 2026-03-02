package org.skriptlang.skript.bukkit.entity.ghast;

import ch.njol.skript.Skript;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.HappyGhast;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.entity.data.SimpleEntityData;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class GhastModule implements AddonModule {

	@Override
	public void load(SkriptAddon addon) {
		SimpleEntityData.addSimpleEntity(Ghast.class, "ghast¦s @a", "ghast[plural:s]");
		if (Skript.classExists("org.bukkit.entity.HappyGhast")) {
			SimpleEntityData.addSimpleEntity(HappyGhast.class, "happy ghast¦s @a", "<age> happy ghast[plural:s]",
				"baby:[happy] ghastling[plural:s]");
		}

		SyntaxRegistry registry = addon.syntaxRegistry();
		CondIsChargingFireball.register(registry);
	}

	@Override
	public String name() {
		return "ghast";
	}

}
