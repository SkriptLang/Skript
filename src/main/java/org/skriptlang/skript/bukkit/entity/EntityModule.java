package org.skriptlang.skript.bukkit.entity;

import ch.njol.skript.Skript;
import ch.njol.skript.entity.SimpleEntityData;
import ch.njol.skript.lang.util.SimpleEvent;
import com.destroystokyo.paper.event.entity.EntityPathfindEvent;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.Location;
import org.bukkit.entity.AbstractNautilus;
import org.bukkit.entity.Entity;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.entity.displays.DisplayModule;
import org.skriptlang.skript.bukkit.entity.elements.expressions.ExprPathfindingLocation;
import org.skriptlang.skript.bukkit.entity.elements.expressions.ExprPathfindingTarget;
import org.skriptlang.skript.bukkit.entity.interactions.InteractionModule;
import org.skriptlang.skript.bukkit.entity.elements.expressions.ExprDeathMessage;
import org.skriptlang.skript.bukkit.entity.entitydata.NautilusData;
import org.skriptlang.skript.bukkit.entity.entitydata.ZombieNautilusData;
import org.skriptlang.skript.bukkit.entity.player.PlayerModule;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
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
		EventValueRegistry registry = addon.registry(EventValueRegistry.class);
		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Pathfind")
			.addDescription("Called whenever an entity tries to pathfind to a location or another entity.")
			.addExample("""
				on pathfind:
				    	broadcast "%event-entity%'s is about to move to %event-location%!"
				""")
			.addSince("INSERT VERSION")
			.addPattern("[entity] [start[s]] pathfind[ing]")
			.addEvent(EntityPathfindEvent.class)
			.build());

		registry.register(EventValue.builder(EntityPathfindEvent.class, Location.class)
			.getter(EntityPathfindEvent::getLoc)
			.patterns("target location")
			.build());

		registry.register(EventValue.builder(EntityPathfindEvent.class, Entity.class)
			.getter(EntityPathfindEvent::getTargetEntity)
			.patterns("target entity")
			.build());

		registry.register(EventValue.builder(EntityPathfindEvent.class, Location.class)
			.getter(event -> event.getEntity().getLocation())
			.build());

		register(addon,
			ExprPathfindingTarget::register,
			ExprPathfindingLocation::register,
			ExprDeathMessage::register
		);
	}

	@Override
	public String name() {
		return "entity";
	}

}
