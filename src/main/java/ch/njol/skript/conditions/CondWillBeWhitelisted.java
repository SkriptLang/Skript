/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Keywords;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.destroystokyo.paper.event.server.WhitelistToggleEvent;
import io.papermc.paper.event.server.WhitelistStateUpdateEvent;
import io.papermc.paper.event.server.WhitelistStateUpdateEvent.WhitelistStatus;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Player / Server Will Be Whitelisted")
@Description({
	"Checks whether a player will be whitelisted in a player whitelist event,",
	"or the server whitelist will be enabled in a server whitelist event."
})
@Examples({
	"if the server will be whitelisted",
	"",
	"if the server whitelist will be enabled:",
	"",
	"if the server whitelist will be disabled:",
	"",
	"if the player will be whitelisted",
})
@Keywords({
	"player",
	"server"
})
@Since("INSERT VERSION")
@RequiredPlugins("Paper (server), Paper 1.20+ (player)")
public class CondWillBeWhitelisted extends Condition {

	private static final boolean HAS_SERVER_WHITELIST_EVENT = Skript.classExists("com.destroystokyo.paper.event.server.WhitelistToggleEvent");
	private static final boolean HAS_PLAYER_WHITELIST_EVENT = Skript.classExists("io.papermc.paper.event.server.WhitelistStateUpdateEvent");

	static {
		// Potentially a 1.13 or earlier event.
		// Suffice to check if server is running with Paper 1.13+.
		if (HAS_SERVER_WHITELIST_EVENT) {
			String type = "server";
			if (HAS_PLAYER_WHITELIST_EVENT)
				type = "player|" + type;
			Skript.registerCondition(CondWillBeWhitelisted.class,
				"the (:" + type + ") (will|not:(will not|would(n't| not))) [be] white[ ]listed",
				"the server whitelist (will|not:(will not|would(n't| not))) [be] (:enable[d]|disable[d])");
		}
	}

	private boolean isPlayer;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		isPlayer = parseResult.hasTag("player");
		if (!isPlayer && HAS_SERVER_WHITELIST_EVENT && getParser().isCurrentEvent(WhitelistToggleEvent.class)) {
			Skript.error("The 'server will be whitelisted' condition can only be used in a 'server whitelist' event");
			return false;
		}
		if (isPlayer && HAS_PLAYER_WHITELIST_EVENT && !getParser().isCurrentEvent(WhitelistStateUpdateEvent.class)) {
			Skript.error("The 'player will be whitelisted' condition can only be used in a 'player whitelist' event");
			return false;
		}
		setNegated(parseResult.hasTag("not"));
		// Pattern whitelist will be enabled/disabled
		if (matchedPattern == 1)
			setNegated(isNegated() ^ !parseResult.hasTag("enable"));
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (!isPlayer && HAS_SERVER_WHITELIST_EVENT && event instanceof WhitelistToggleEvent)
			return (((WhitelistToggleEvent) event).isEnabled()) ^ isNegated();
		if (isPlayer && HAS_PLAYER_WHITELIST_EVENT && event instanceof WhitelistStateUpdateEvent) {
			boolean added = ((WhitelistStateUpdateEvent) event).getStatus() == WhitelistStatus.ADDED;
			return added ^ isNegated();
		}
		return false;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (isPlayer ? "player" : "server") + (isNegated() ? " will" : " will not") + " be whitelisted";
	}

}
