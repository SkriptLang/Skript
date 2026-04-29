package org.skriptlang.skript.bukkit.entity;

import ch.njol.skript.Skript;
import ch.njol.skript.entity.SimpleEntityData;
import ch.njol.skript.lang.util.SimpleEvent;
import org.bukkit.entity.AbstractNautilus;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.entity.displays.DisplayModule;
import org.skriptlang.skript.bukkit.entity.elements.effects.EffGlide;
import org.skriptlang.skript.bukkit.entity.interactions.InteractionModule;
import org.skriptlang.skript.bukkit.entity.elements.expressions.ExprDeathMessage;
import org.skriptlang.skript.bukkit.entity.entitydata.NautilusData;
import org.skriptlang.skript.bukkit.entity.entitydata.ZombieNautilusData;
import org.skriptlang.skript.bukkit.entity.player.PlayerModule;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

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
	protected void loadSelf(SkriptAddon addon) {
		if (Skript.classExists("org.bukkit.entity.Nautilus")) {
			NautilusData.register();
			ZombieNautilusData.register();
			SimpleEntityData.addSuperEntity("any nautilus", AbstractNautilus.class);
		}
		SyntaxRegistry syntaxRegistry = moduleRegistry(addon);
		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Toggle Glide")
			.addDescription("Called when an entity starts or stops gliding, or when the server toggles the gliding state of an entity forcibly.")
			.addExample("""
				on toggling gliding:
					cancel the event # bad idea, but you CAN do it!
				""")
			.addSince("2.2-dev21")
			.addSince("INSERT VERSION","Pattern changed")
			.addPatterns(
				"[on] gliding state change",
				"[on] (toggle|toggling) glid(e|ing)",
				"[on] glid(e|ing) toggl(e|d)"
			)
			.addEvent(EntityToggleGlideEvent.class)
			.build());
		register(addon,
			ExprDeathMessage::register,
			EffGlide::register
		);
	}

	@Override
	public String name() {
		return "entity";
	}

}
