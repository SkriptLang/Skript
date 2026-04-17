package org.skriptlang.skript.bukkit.entity.nautilus;

import ch.njol.skript.Skript;
import org.bukkit.entity.AbstractNautilus;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.entity.data.SimpleEntityData;

public class NautilusModule extends HierarchicalAddonModule {

	public NautilusModule(AddonModule parentModule) {
		super(parentModule);
	}

	@Override
	protected boolean canLoadSelf(SkriptAddon addon) {
		return Skript.classExists("org.bukkit.entity.Nautilus");
	}

	@Override
	protected void loadSelf(SkriptAddon addon) {
		NautilusData.register();
		ZombieNautilusData.register();
		SimpleEntityData.addSuperEntity(AbstractNautilus.class, "any nautilus¦es @an", "any <age> nautilus[plural:es]");
	}

	@Override
	public String name() {
		return "nautilus";
	}

}
