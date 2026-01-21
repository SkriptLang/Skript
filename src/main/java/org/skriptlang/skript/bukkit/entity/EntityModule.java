package org.skriptlang.skript.bukkit.entity;

import ch.njol.skript.Skript;
import ch.njol.skript.entity.SimpleEntityData;
import org.bukkit.entity.AbstractNautilus;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.entity.nautilus.NautilusData;
import org.skriptlang.skript.bukkit.entity.nautilus.ZombieNautilusData;
import org.skriptlang.skript.bukkit.entity.player.PlayerModule;
import org.skriptlang.skript.bukkit.entity.misc.*;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.ArrayList;
import java.util.List;

public class EntityModule implements AddonModule {

	private List<AddonModule> subModules = new ArrayList<>(List.of(
		new PlayerModule()
	));

	@Override
	public boolean canLoad(SkriptAddon addon) {
		subModules.removeIf(module -> !module.canLoad(addon));
		return !subModules.isEmpty();
	}

	@Override
	public void init(SkriptAddon addon) {
		for (AddonModule module : subModules) {
			module.init(addon);
		}
	}

	@Override
	public void load(SkriptAddon addon) {
		for (AddonModule module : subModules) {
			module.load(addon);
		}
		subModules = null;

		if (Skript.classExists("org.bukkit.entity.Nautilus")) {
			NautilusData.register();
			ZombieNautilusData.register();
			SimpleEntityData.addSuperEntity("any nautilus", AbstractNautilus.class);
		}

		// misc
		SyntaxRegistry syntaxRegistry = addon.syntaxRegistry();
		ExprDeathMessage.register(syntaxRegistry);
	}

	@Override
	public String name() {
		return "entity";
	}

}
