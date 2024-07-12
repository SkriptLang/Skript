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
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Is Whitelisted")
@Description("Whether or not the server or a player is whitelisted, or the server is whitelist enforced.")
@Examples({
	"if the player is whitelisted:",
	"if the server is whitelisted:",
	"if the server whitelist is enforced:"
})
@Since("2.5.2, 2.9.0 (enforce, offline players)")
@RequiredPlugins("MC 1.17+ (enforce)")
public class CondIsWhitelisted extends Condition {

	private static final boolean ENFORCE_SUPPORT = Skript.methodExists(Bukkit.class, "isWhitelistEnforced");

	static {
		String[] patterns = new String[ENFORCE_SUPPORT ? 3 : 2];
		patterns[0] = "[the] server (is|not:(isn't|is not)) (in white[ ]list mode|white[ ]listed)";
		patterns[1] = "%offlineplayers% (is|are|not:(isn't|is not|aren't|are not)) white[ ]listed";
		if (ENFORCE_SUPPORT)
			patterns[2] = "[the] server white[ ]list (is|not:(isn't|is not)) enforced";
		Skript.registerCondition(CondIsWhitelisted.class, patterns);
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
		String negation = isNegated() ? "not" : "";
		if (isServer) {
			if (isEnforce) {
				return "the server whitelist is " + negation + " enforced";
			}
			return "the server is " + negation + " whitelisted";
		}
		return players.toString(event, debug) + " is " + negation + " whitelisted";
	}

}
