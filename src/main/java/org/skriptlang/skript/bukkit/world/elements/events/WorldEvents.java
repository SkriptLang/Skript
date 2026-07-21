package org.skriptlang.skript.bukkit.world.elements.events;

import ch.njol.skript.lang.util.SimpleEvent;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.event.world.*;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class WorldEvents {

	public static void register(SyntaxRegistry syntaxRegistry, EventValueRegistry eventValueRegistry) {

		// World Event Values
		eventValueRegistry.register(EventValue.builder(WorldEvent.class, World.class)
			.getter(WorldEvent::getWorld)
			.build());

		eventValueRegistry.register(EventValue.builder(ChunkEvent.class, Chunk.class)
			.getter(ChunkEvent::getChunk)
			.build());

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
				.addPatterns("[on] chunk (generat|populat)(e|ing)")
				.addDescription("Called after a chunk has been generated for first time.")
				.addExample("""
					on chunk generate:
					    broadcast "A chunk has been newly generated!"
					""")
				.addSince("1.0")
				.supplier(() -> new SimpleEvent("chunk generate"))
				.build()
		);

		syntaxRegistry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "World Spawn Change")
				.addEvent(SpawnChangeEvent.class)
				.addPatterns("[world] spawn change")
				.addDescription("Called when the spawn point of a world changes.")
				.addExample("""
					on world spawn change:
					    broadcast "Someone changed the world spawn!"
					""")
				.addSince("1.0")
				.supplier(() -> new SimpleEvent("world spawn change"))
				.build()
		);

		syntaxRegistry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Lightning Strike")
				.addEvent(LightningStrikeEvent.class)
				.addPatterns("lightning [strik(e|ing]")
				.addDescription("Called when lightning strikes in a world.")
				.addExample("""
					on lightning strike:
					    spawn a zombie at event-location
					""")
				.addSince("1.0, INSERT VERSION (pattern change)")
				.supplier(() -> new SimpleEvent("lightning strike"))
				.build()
		);

		eventValueRegistry.register(EventValue.builder(LightningStrikeEvent.class, Entity.class)
			.getter(LightningStrikeEvent::getLightning)
			.build());
	}
}
