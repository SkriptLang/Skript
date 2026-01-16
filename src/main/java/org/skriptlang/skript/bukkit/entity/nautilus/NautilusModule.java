package org.skriptlang.skript.bukkit.entity.nautilus;

import ch.njol.skript.Skript;
import org.bukkit.entity.AbstractNautilus;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.entity.data.SimpleEntityData;

public class NautilusModule implements AddonModule {

	@Override
	public boolean canLoad(SkriptAddon addon) {
		return Skript.classExists("org.bukkit.entity.Nautilus");
	}

	@Override
	public void load(SkriptAddon addon) {
		NautilusData.register();
		ZombieNautilusData.register();
		SimpleEntityData.addSuperEntity("any nautilus", AbstractNautilus.class);
	}

	@Override
	public String name() {
		return "nautilus";
	}

}
