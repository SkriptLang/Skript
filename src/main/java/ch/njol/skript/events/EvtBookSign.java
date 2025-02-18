package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.jetbrains.annotations.Nullable;

public class EvtBookSign extends SkriptEvent{
	
	static {
		Skript.registerEvent("Book Sign", EvtBookSign.class, PlayerEditBookEvent.class, "book sign[ing]")
				.description("Called when a player signs a book.")
				.examples("on book sign:")
				.since("2.2-dev31");
	}
	
	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, SkriptParser.ParseResult parseResult) {
		return true;
	}
	
	@Override
	public boolean check(Event e) {
		if (!(e instanceof PlayerEditBookEvent)){
			return false;
		}
		return ((PlayerEditBookEvent) e).isSigning();
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "book sign";
	}
}
