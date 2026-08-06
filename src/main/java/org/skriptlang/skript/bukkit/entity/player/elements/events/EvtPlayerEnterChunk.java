package org.skriptlang.skript.bukkit.entity.player.elements.events;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EvtPlayerEnterChunk extends SkriptEvent {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(EvtPlayerEnterChunk.class, "Player Enter Chunk")
			.supplier(EvtPlayerEnterChunk::new)
			.addEvent(PlayerMoveEvent.class)
			.addPatterns("[player] (enter[s] [a] chunk|chunk enter[ing])")
			.addDescription("""
				Called when a player enters a chunk.
				Note that this event is based on 'player move' event, and may be called frequently internally.
				""")
			.addExample("""
				on player enter a chunk:
					send "You entered a chunk: %past event-chunk% -> %event-chunk%!" to player"
				""")
			.addSince("2.7")
			.build());
	}

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		return true;
	}

	@Override
	public boolean check(Event event) {
		PlayerMoveEvent playerEvent = (PlayerMoveEvent) event;

		return !playerEvent.getFrom().getChunk().equals(playerEvent.getTo().getChunk());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "player enter chunk";
	}

}
