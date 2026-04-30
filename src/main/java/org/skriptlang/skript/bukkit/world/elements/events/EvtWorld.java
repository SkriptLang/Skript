package org.skriptlang.skript.bukkit.world.elements.events;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.bukkit.event.Event;

import org.bukkit.event.world.*;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EvtWorld extends SkriptEvent {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtWorld.class, "Chunk Load")
				.addEvent(ChunkLoadEvent.class)
				.addPatterns("chunk load[ing]")
				.addDescription("Called when a chunk loads. The chunk might or might not contain mobs when it's loaded.")
				.addExample("on chunk load:")
				.addSince("1.0")
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtWorld.class, "Chunk Generate")
				.addEvent(ChunkPopulateEvent.class)
				.addPatterns("chunk (generat|populat)(e|ing)")
				.addDescription("Called after a new chunk was generated.")
				.addExample("on chunk generate:")
				.addSince("1.0")
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtWorld.class, "Chunk Unload")
				.addEvent(ChunkUnloadEvent.class)
				.addPatterns("chunk unload[ing]")
				.addDescription("Called when a chunk is unloaded due to not being near any player.")
				.addExample("on chunk unload:")
				.addSince("1.0")
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtWorld.class, "Portal Create")
				.addEvent(PortalCreateEvent.class)
				.addPatterns("portal creat(e|ion)")
				.addDescription("Called when a portal is created, either by a player or mob lighting an obsidian frame on fire, or by a nether portal creating its teleportation target in the nether/overworld.",
					"In Minecraft 1.14+, you can use <a href='#ExprEntity'>the player</a> in this event.", "Please note that there may not always be a player (or other entity) in this event.")
				.addExample("on portal create:")
				.addRequiredPlugins("Minecraft 1.14+ (event-entity support)")
				.addSince("1.0, 2.5.3 (event-entity support)")
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtWorld.class, "Spawn Change")
				.addEvent(SpawnChangeEvent.class)
				.addPatterns("[world] spawn change")
				.addDescription("Called when the spawn point of a world changes.")
				.addExample(
					"""
						on spawn change:
							broadcast "someone changed the spawn!"
						""")
				.addSince("1.0")
				.build()
		);
	}

	@Override
	public boolean init(Literal<?>[] exprs, int matchedPattern, ParseResult parseResult) {
		return true;
	}

	@Override
	public boolean check(Event event) {
		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "world event";
	}

}
