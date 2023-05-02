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

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

import java.util.ArrayList;
import java.util.List;

@Name("Is Whitelisted")
@Description("Whether or not the server or a player is whitelisted, or the server is whitelist enforced.")
@Examples({
	"if player is whitelisted:",
	"if server is whitelisted:",
	"if server is whitelist enforced:"
})
@Since("2.5.2, INSERT VERSION (enforce, offline players)")
@RequiredPlugins("MC 1.17+ (enforce)")
public class CondIsWhitelisted extends Condition {

	private static final boolean ENFORCE_SUPPORT = Skript.methodExists(Bukkit.class, "isWhitelistEnforced");

	static {
		List<String> patterns = new ArrayList<>();
		patterns.add("[the] server (is|not:(isn't|is not)) white[ ]listed");
		patterns.add("%offlineplayers% (is|are|not:(isn't|is not|aren't|are not)) white[ ]listed");
		if (ENFORCE_SUPPORT)
			patterns.add("[the] white[ ]list (is|not:(isn't|is not)) enforced");
		Skript.registerCondition(CondIsWhitelisted.class, patterns.toArray(new String[0]));
	}

	@Nullable
	private Expression<OfflinePlayer> players;

	private boolean isServer;
	private boolean isEnforce;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setNegated(parseResult.hasTag("not"));
		isServer = matchedPattern != 1;
		isEnforce = matchedPattern == 2;
		if (matchedPattern == 1)
			players = (Expression<OfflinePlayer>) exprs[0];
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (isServer)
			return (isEnforce ? Bukkit.isWhitelistEnforced() : Bukkit.hasWhitelist()) ^ isNegated();
		return players.check(event, OfflinePlayer::isWhitelisted, isNegated());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (players != null ? players.toString(event, debug) : "server") + " is " + (isNegated() ? "not " : " ")
				+ (isEnforce ? "whitelist enforced" : "whitelisted");
	}

}
