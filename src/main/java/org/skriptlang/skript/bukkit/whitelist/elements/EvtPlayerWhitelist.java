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
import ch.njol.util.Kleenean;

public class EvtPlayerWhitelist extends SkriptEvent {

	static {
		Skript.registerEvent("Player Whitelist", EvtPlayerWhitelist.class, WhitelistStateUpdateEvent.class,
				"player whitelist [state] (change[d]|toggle[d]|update[d])",
				"player (added to whitelist|whitelist[ed])",
				"player (removed from whitelist|unwhitelist[ed])")
			.description(
				"Called whenever a player has been added to or removed from the server's whitelist.",
				"Use <a href='conditions.html#CondWillBeWhitelisted'>will be whitelisted</a> condition to check with its state.")
			.examples(
				"on player whitelisted:",
				"on player unwhitelisted:",
				"",
				"on player whitelist toggled:",
					"\tsend \"Whitelist of player %event-offlineplayer% has been set to %whether server will be whitelisted%\" to all ops")
			.since("");

		EventValues.registerEventValue(WhitelistStateUpdateEvent.class, OfflinePlayer.class, WhitelistStateUpdateEvent::getPlayer);
	}

	private Kleenean state = Kleenean.UNKNOWN;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		if (matchedPattern == 1)
			state = Kleenean.TRUE;
		else if (matchedPattern == 2)
			state = Kleenean.FALSE;
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (!state.isUnknown())
			return state.isTrue() == (((WhitelistStateUpdateEvent) event).getStatus() == WhitelistStatus.ADDED);
		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug)
			.append("player");
		if (!state.isUnknown()) {
			builder.append(state.isTrue() ? "added to" : "removed from")
				.append("whitelist");
		} else {
			builder.append("whitelist toggled");
		}
		return builder.toString();
	}

}
