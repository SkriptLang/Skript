package org.skriptlang.skript.bukkit.entity.player.elements.events;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EvtPlayerFirstJoin extends SkriptEvent {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(EvtPlayerFirstJoin.class, "Player First Join")
			.supplier(EvtPlayerFirstJoin::new)
			.addEvent(PlayerJoinEvent.class)
			.addPatterns("[player] first (join|log[ging ]in)")
			.addDescription("Called when a player joins the server for the first time.")
			.addExample("""
				on player first join:
					set the join message to "[NEW] %player%!"
				""")
			.addSince("1.3.7")
			.addSince("INSERT VERSION ('player' and 'logging in' in pattern)")
			.build());
	}

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		return true;
	}

	@Override
	public boolean check(Event event) {
		PlayerJoinEvent playerEvent = (PlayerJoinEvent) event;

		return !playerEvent.getPlayer().hasPlayedBefore();
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "player first join";
	}

}
