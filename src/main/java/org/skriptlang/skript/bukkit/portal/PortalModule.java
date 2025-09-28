package org.skriptlang.skript.bukkit.portal;

import org.bukkit.Location;
import org.bukkit.PortalType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.entity.EntityPortalExitEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.util.Vector;

import java.io.IOException;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.util.SimpleEvent;
import ch.njol.skript.registrations.EventConverter;
import ch.njol.skript.registrations.EventValues;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;

public class PortalModule implements AddonModule {

	@Override
	public void load(SkriptAddon addon) {
		try {
			Skript.getAddonInstance().loadClasses("org.skriptlang.skript.bukkit.portal", "elements");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		Skript.registerEvent("Portal Create", SimpleEvent.class, PortalCreateEvent.class, "portal creat(e|ion)")
			.description("Called when a portal is created, either by a player or mob lighting an obsidian frame on fire, or by a nether portal creating its teleportation target in the nether/overworld.",
				"In Minecraft 1.14+, you can use <a href='#ExprEntity'>the player</a> in this event.", "Please note that there may not always be a player (or other entity) in this event.")
			.examples("on portal create:")
			.since("1.0, 2.5.3 (event-entity support)");
		EventValues.registerEventValue(PortalCreateEvent.class, Block[].class, event -> event.getBlocks().stream()
			.map(BlockState::getBlock)
			.toArray(Block[]::new));
		EventValues.registerEventValue(PortalCreateEvent.class, Entity.class, PortalCreateEvent::getEntity);
		EventValues.registerEventValue(PortalCreateEvent.class, PortalType.class, event -> switch (event.getReason()) {
			case END_PLATFORM -> PortalType.ENDER;
			case FIRE, NETHER_PAIR -> PortalType.NETHER;
		});

		Skript.registerEvent("Portal Enter", SimpleEvent.class, EntityPortalEnterEvent.class, "portal enter[ing]", "entering [a] portal")
			.description("Called when an entity enters a nether portal or an end portal. Please note that this event will be fired many times for a nether portal.")
			.examples("on portal enter:")
			.since("1.0, INSERT VERSION (event values)");
		EventValues.registerEventValue(EntityPortalEnterEvent.class, Location.class, EntityPortalEnterEvent::getLocation);
		EventValues.registerEventValue(EntityPortalEnterEvent.class, PortalType.class, EntityPortalEnterEvent::getPortalType);

		Skript.registerEvent("Portal Exit", SimpleEvent.class, EntityPortalExitEvent.class, "portal exit[ing]", "exiting [a] portal")
			.description("Called when an entity exits a nether portal or an end portal. Note that this event does not get called on players.")
			.examples("on portal exit:")
			.since("INSERT VERSION");
		EventValues.registerEventValue(EntityPortalExitEvent.class, Vector.class, EntityPortalExitEvent::getBefore, EventValues.TIME_PAST);
		EventValues.registerEventValue(EntityPortalExitEvent.class, Vector.class, new EventConverter<>() {
			@Override
			public void set(EntityPortalExitEvent event, Vector vector) {
				event.setAfter(vector);
			}

			@Override
			public Vector convert(EntityPortalExitEvent event) {
				return event.getAfter();
			}
		});
	}

}
