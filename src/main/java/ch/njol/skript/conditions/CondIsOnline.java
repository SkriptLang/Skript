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
import org.bukkit.OfflinePlayer;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Is Online")
@Description("Checks whether a player is online. The 'connected' pattern will return false once this player leaves the server, even if they rejoin. Be aware that using the 'connected' pattern with a variable will not have this special behavior. Use the direct event-player or other non-variable expression for best results.")
@Examples({
	"player is online",
	"player-argument is offline",
	"while player is connected:",
		"\twait 60 seconds",
		"\tsend \"hello!\" to player",
	"",
	"# The following will act like `{_player} is online`.",
	"# Using variables with `is connected` will not behave the same as with non-variables.",
	"while {_player} is connected:",
	    "\tbroadcast \"online!\"",
	    "\twait 1 tick"
})
@Since("1.4")
@RequiredPlugins("Paper 1.20+ (Connected)")
public class CondIsOnline extends PropertyCondition<OfflinePlayer> {
	
	static {
		if (Skript.methodExists(OfflinePlayer.class, "isConnected"))
			register(CondIsOnline.class, "(online|:offline|:connected)", "offlineplayers");
		else
			register(CondIsOnline.class, "(online|:offline)", "offlineplayers");
	}
	
	private boolean connected; // https://github.com/SkriptLang/Skript/issues/6100
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		this.setExpr((Expression<OfflinePlayer>) exprs[0]);
		this.setNegated(matchedPattern == 1 ^ parseResult.hasTag("offline"));
		this.connected = parseResult.hasTag("connected");
		return true;
	}
	
	@Override
	public boolean check(OfflinePlayer op) {
		if (connected)
			return op.isConnected();
		return op.isOnline();
	}
	
	@Override
	protected String getPropertyName() {
		return connected ? "connected" : "online";
	}
	
}
