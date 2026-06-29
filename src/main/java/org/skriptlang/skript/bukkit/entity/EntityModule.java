package org.skriptlang.skript.bukkit.entity;

import ch.njol.skript.Skript;
import ch.njol.skript.entity.SimpleEntityData;
import ch.njol.skript.registrations.Classes;
import org.bukkit.entity.AbstractNautilus;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.entity.displays.DisplayModule;
import org.skriptlang.skript.bukkit.entity.elements.effects.EffTeleport;
import org.skriptlang.skript.bukkit.entity.interactions.InteractionModule;
import org.skriptlang.skript.bukkit.entity.elements.expressions.ExprDeathMessage;
import org.skriptlang.skript.bukkit.entity.entitydata.NautilusData;
import org.skriptlang.skript.bukkit.entity.entitydata.ZombieNautilusData;
import org.skriptlang.skript.bukkit.entity.player.PlayerModule;
import org.skriptlang.skript.bukkit.entity.types.TeleportFlagClassInfo;

import java.util.List;

public class EntityModule extends HierarchicalAddonModule {

	public EntityModule(AddonModule parentModule) {
		super(parentModule);
	}

	public Iterable<AddonModule> children() {
		return List.of(
			new DisplayModule(this),
			new InteractionModule(this),
			new PlayerModule(this)
		);
	}

	@Override
	protected void initSelf(SkriptAddon addon) {
		Classes.registerClass(new TeleportFlagClassInfo());
	}

	@Override
	protected void loadSelf(SkriptAddon addon) {
		if (Skript.classExists("org.bukkit.entity.Nautilus")) {
			NautilusData.register();
			ZombieNautilusData.register();
			SimpleEntityData.addSuperEntity("any nautilus", AbstractNautilus.class);
		}

		register(addon,
			EffTeleport::register,
			ExprDeathMessage::register
		);
	}

	@Override
	public String name() {
		return "entity";
	}

}
