package org.skriptlang.skript.bukkit.misc.elements.events;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser;
import org.bukkit.event.Event;
import org.bukkit.event.server.BroadcastMessageEvent;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EvtBroadcastMessage extends SkriptEvent {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtBroadcastMessage.class, "Broadcast Message")
				.addEvent(BroadcastMessageEvent.class)
				.addPatterns("broadcast")
				.addDescription("Called when a message is broadcasted.")
				.addExample("""
					on broadcast:
						set broadcast-message to "&c[BROADCAST] %broadcasted message%"
					"""
				)
				.addSince("2.10")
				.supplier(EvtBroadcastMessage::new)
				.build()
		);

	}
	@Override
	public boolean init(Literal<?>[] exprs, int matchedPattern, SkriptParser.ParseResult parseResult) {
		return true;
	}

	@Override
	public boolean check(Event event) {
		return true;
	}

	@Override
	public String toString(Event event, boolean debug) { return "broadcast event"; }

}
