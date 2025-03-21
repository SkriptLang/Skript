package ch.njol.skript.events;

import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;

/**
 * @author Peter Güttinger
 */
public class EvtFirstJoin extends SkriptEvent {
	static {
		Skript.registerEvent("First Join", EvtFirstJoin.class, PlayerJoinEvent.class, "first (join|login)")
				.description("Called when a player joins the server for the first time.")
				.examples("on first join:",
						"\tbroadcast \"Welcome %player% to the server!\"")
				.since("1.3.7");
	}
	
	@Override
	public boolean init(final Literal<?>[] args, final int matchedPattern, final ParseResult parser) {
		return true;
	}
	
	@Override
	public boolean check(final Event e) {
		return !((PlayerJoinEvent) e).getPlayer().hasPlayedBefore();
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "first join";
	}
	
}
