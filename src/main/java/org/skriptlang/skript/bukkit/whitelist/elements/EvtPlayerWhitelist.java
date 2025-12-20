package org.skriptlang.skript.bukkit.whitelist.elements;

import io.papermc.paper.event.server.WhitelistStateUpdateEvent;
import io.papermc.paper.event.server.WhitelistStateUpdateEvent.WhitelistStatus;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.registrations.EventValues;

public class EvtPlayerWhitelist extends SkriptEvent {

	private enum EventState {

		WHITELISTED("whitelisted"),
		UNWHITELISTED("unwhitelisted");

		final String toString;

		EventState(String toString) {
			this.toString = toString;
		}

		@Override
		public String toString() {
			return toString;
		}

	}

	static {
		Skript.registerEvent("Player Whitelist", EvtPlayerWhitelist.class, WhitelistStateUpdateEvent.class,
				"player whitelist [state] (change[d]|update[d])",
				"player (added to whitelist|whitelist[ed])",
				"player (removed from whitelist|unwhitelist[ed])")
			.description(
				"Called whenever a player has been added to or removed from the server's whitelist.",
				"Use <a href='conditions.html#CondWillBeWhitelisted'>will be whitelisted</a> condition to check with its state.")
			.examples(
				"on player whitelisted:",
				"on player unwhitelisted:",
				"",
				"on player whitelist state changed:",
					"\tsend \"Whitelist of player %event-offlineplayer% has been set to %whether server will be whitelisted%\" to all ops")
			.since("INSERT VERSION");

		EventValues.registerEventValue(WhitelistStateUpdateEvent.class, OfflinePlayer.class, WhitelistStateUpdateEvent::getPlayer);
	}

	private @Nullable EventState state = null;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		if (matchedPattern == 1)
			state = EventState.WHITELISTED;
		else if (matchedPattern == 2)
			state = EventState.UNWHITELISTED;
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (state != null)
			return (state == EventState.WHITELISTED) == (((WhitelistStateUpdateEvent) event).getStatus() == WhitelistStatus.ADDED);
		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug)
			.append("player");
		if (state != null) {
			if (state == EventState.WHITELISTED)
				builder.append("added to whitelist");
			else
				builder.append("removed from whitelist");
		} else {
			builder.append("whitelist state changed");
		}
		return builder.toString();
	}

}
