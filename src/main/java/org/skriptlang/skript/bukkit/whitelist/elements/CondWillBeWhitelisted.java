package org.skriptlang.skript.bukkit.whitelist.elements;

import com.destroystokyo.paper.event.server.WhitelistToggleEvent;
import io.papermc.paper.event.server.WhitelistStateUpdateEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;

@Name("Will Be Whitelisted")
@Description("Checks whether the server or a player will be whitelisted in a <a href='events.html#whitelist'>whitelist</a> event.")
@Keywords("server, player")
@Examples({
	"on server whitelist:",
	"\tsend \"Server whitelist has been set to % whether server will be whitelisted%\" to all ops",
	"",
	"on player whitelist:",
	"\tsend \"Whitelist of player % event - player % has been set to % whether server will be whitelisted%\" to all ops"
})
@Since("INSERT VERSION")
@RequiredPlugins("Paper (server), Paper 1.20+ (player)")
public class CondWillBeWhitelisted extends Condition {

	static {
		Skript.registerCondition(CondWillBeWhitelisted.class,
			"[the] (:player|server) will be whitelisted",
			"[the] (:player|server) (will not|won't) be whitelisted"
		);
	}

	private boolean isServer;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		isServer = !parseResult.hasTag("player");
		if (isServer) {
			if (!getParser().isCurrentEvent(WhitelistToggleEvent.class)) {
				Skript.error("The 'server will be whitelisted' condition can only be used in an 'server whitelist' event");
				return false;
			}
		} else {
			if (!getParser().isCurrentEvent(WhitelistStateUpdateEvent.class)) {
				Skript.error("The 'player will be whitelisted' condition can only be used in an 'player whitelist' event");
				return false;
			}
		}
		setNegated(matchedPattern == 1);
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (isServer)
			return ((WhitelistToggleEvent) event).isEnabled() ^ isNegated();
		return (((WhitelistStateUpdateEvent) event).getStatus() == WhitelistStateUpdateEvent.WhitelistStatus.ADDED) ^ isNegated();
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append("the")
			.append(isServer ? "server" : "player")
			.append("will be whitelisted")
			.toString();
	}

}
