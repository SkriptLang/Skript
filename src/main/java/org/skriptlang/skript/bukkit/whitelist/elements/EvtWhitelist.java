package org.skriptlang.skript.bukkit.whitelist.elements;

import com.destroystokyo.paper.event.server.WhitelistToggleEvent;
import io.papermc.paper.event.server.WhitelistStateUpdateEvent;
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
import ch.njol.util.coll.CollectionUtils;

public class EvtWhitelist extends SkriptEvent {

	static {
		Skript.registerEvent("Whitelist", EvtWhitelist.class, CollectionUtils.array(WhitelistToggleEvent.class, WhitelistStateUpdateEvent.class),
				"server whitelist [state:(:enable[d]|disable[d])]",
				"player whitelist [state:(:add[ed]|remove[d])]")
			.description(
				"Called whenever the server's or a player's whitelist state has been changed.",
				"Use <a href='conditions.html#CondWillBeWhitelisted'>will be whitelist</a> condition to check with its state.")
			.keywords("player", "server")
			.examples(
				"on server whitelist enabled:",
				"on server whitelist disabled:",
				"on player whitelist added:",
				"on player whitelist removed:",
				"",
				"on server whitelist:",
					"\tsend \"Server whitelist has been set to %whether server will be whitelisted%\" to all ops",
				"",
				"on player whitelist:",
					"\tsend \"Whitelist of player %event-player% has been set to %whether server will be whitelisted%\" to all ops")
			.since("INSERT VERSION")
			.requiredPlugins("Paper");

		EventValues.registerEventValue(WhitelistStateUpdateEvent.class, OfflinePlayer.class, WhitelistStateUpdateEvent::getPlayer);
	}

	private boolean isServer;
	private Kleenean state;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		isServer = matchedPattern == 0;
		state = Kleenean.UNKNOWN;
		if (parseResult.hasTag("state")) {
			if (isServer)
				state = Kleenean.get(parseResult.hasTag("enable"));
			else
				state = Kleenean.get(parseResult.hasTag("add"));
		}
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (isServer) {
			if (!(event instanceof WhitelistToggleEvent serverWhitelist))
				return false;
			if (!state.isUnknown())
				return state.isTrue() == serverWhitelist.isEnabled();
		} else {
			if (!(event instanceof WhitelistStateUpdateEvent playerWhitelist))
				return false;
			if (!state.isUnknown())
				return state.isTrue() == (playerWhitelist.getStatus() == WhitelistStateUpdateEvent.WhitelistStatus.ADDED);
		}
		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug)
			.append(isServer ? "server" : "player")
			.append("whitelist");
		if (!state.isUnknown()) {
			if (isServer)
				builder.append(state.isTrue() ? "enabled" : "disabled");
			else
				builder.append(state.isTrue() ? "added" : "removed");
		}
		return builder.toString();
	}

}
