package org.skriptlang.skript.bukkit.entity.player.elements.events;

import org.bukkit.event.player.PlayerEditBookEvent;
import org.jetbrains.annotations.Nullable;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.bukkit.event.Event;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EvtPlayerSignBook extends SkriptEvent {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(EvtPlayerSignBook.class, "Player Sign Book")
			.supplier(EvtPlayerSignBook::new)
			.addEvent(PlayerEditBookEvent.class)
			.addPatterns("book sign[ing]", "[player] sign[ing] book")
			.addDescription("Called when a player signs a book.")
			.addExample("""
				on player sign book:
					send "Now everyone will know you wrote %event-item stack%<reset>!" to player
				""")
			.addSince("2.2-dev31")
			.addSince("INSERT VERSION (added pattern)")
			.build());
	}

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		return true;
	}

	@Override
	public boolean check(Event event) {
		PlayerEditBookEvent playerEvent = (PlayerEditBookEvent) event;

		return playerEvent.isSigning();
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "player sign book";
	}

}
