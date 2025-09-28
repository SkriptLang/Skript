package org.skriptlang.skript.bukkit.portal;

import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.world.PortalCreateEvent;

import java.io.IOException;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.util.SimpleEvent;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;

public class PortalModule implements AddonModule {

	@Override
	public void load(SkriptAddon addon) {
		try {
			Skript.getAddonInstance().loadClasses("org.skriptlang.skript.bukkit.portal", "elements");

			Skript.registerEvent("Portal Enter", SimpleEvent.class, EntityPortalEnterEvent.class, "portal enter[ing]", "entering [a] portal")
				.description("Called when an entity enters a nether portal or an end portal. Please note that this event will be fired many times for a nether portal.")
				.examples("on portal enter:")
				.since("1.0");

			Skript.registerEvent("Portal Create", SimpleEvent.class, PortalCreateEvent.class, "portal creat(e|ion)")
				.description("Called when a portal is created, either by a player or mob lighting an obsidian frame on fire, or by a nether portal creating its teleportation target in the nether/overworld.",
					"In Minecraft 1.14+, you can use <a href='#ExprEntity'>the player</a> in this event.", "Please note that there may not always be a player (or other entity) in this event.")
				.examples("on portal create:")
				.since("1.0, 2.5.3 (event-entity support)");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
