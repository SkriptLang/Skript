package org.skriptlang.skript.bukkit.world.elements.events;

import ch.njol.skript.lang.util.SimpleEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkPopulateEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class WorldEvents {

	public static void register(SyntaxRegistry syntaxRegistry, EventValueRegistry eventValueRegistry) {
		syntaxRegistry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Chunk Load")
				.addEvent(ChunkLoadEvent.class)
				.addPatterns("chunk load[ing]")
				.addDescription("""	
					Called when a chunk loads.
					Note that the chunk may have entities in it.
					""")
				.addExample("""
					on chunk load:
					    broadcast "A chunk has loaded!"
					""")
				.addSince("1.0")
				.supplier(() -> new SimpleEvent("chunk load"))
				.build()
		);

		syntaxRegistry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Chunk Unload")
				.addEvent(ChunkUnloadEvent.class)
				.addPatterns("chunk unload[ing]")
				.addDescription("Called when a chunk is unloaded due to not being near any player.")
				.addExample("""
					on chunk unload:
					    broadcast "A chunk is being unloaded!"
					""")
				.addSince("1.0")
				.supplier(() -> new SimpleEvent("chunk unload"))
				.build()
		);

		syntaxRegistry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Chunk Generate")
				.addEvent(ChunkPopulateEvent.class)
				.addPatterns("chunk unload[ing]")
				.addDescription("Called after a chunk has been generated for first time.")
				.addExample("""
					on chunk generate:
					    broadcast "A chunk has been newly generated!"
					""")
				.addSince("1.0")
				.supplier(() -> new SimpleEvent("chunk generate"))
				.build()
		);

	}
}
