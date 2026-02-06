package org.skriptlang.skript.bukkit.entity;

import ch.njol.skript.Skript;
import ch.njol.skript.entity.SimpleEntityData;
import org.bukkit.entity.AbstractNautilus;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.entity.displays.DisplayModule;
import org.skriptlang.skript.bukkit.entity.interactions.InteractionModule;
import org.skriptlang.skript.bukkit.entity.nautilus.NautilusData;
import org.skriptlang.skript.bukkit.entity.nautilus.ZombieNautilusData;

import java.util.List;

public class EntityModule extends HierarchicalAddonModule {

	public EntityModule(AddonModule parentModule) {
		super(parentModule);
	}

	@Override
	public Iterable<AddonModule> children() {
		return List.of(
			new DisplayModule(this),
			new InteractionModule(this)
		);
	}

	@Override
	protected void loadSelf(SkriptAddon addon) {
		if (Skript.classExists("org.bukkit.entity.Nautilus")) {
			NautilusData.register();
			ZombieNautilusData.register();
			SimpleEntityData.addSuperEntity("any nautilus", AbstractNautilus.class);
		}
	}

	@Override
	public String name() {
		return "entity";
	}

}
