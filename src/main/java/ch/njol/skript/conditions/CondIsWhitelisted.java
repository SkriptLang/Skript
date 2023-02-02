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

@Name("Is Whitelisted")
@Description("Whether or not the server or a player is whitelisted, and server is whitelist enforced.")
@Examples({
	"if player is whitelisted:",
	"if server is whitelisted:",
	"if server is whitelist enforced:"
})
@Since("2.5.2, INSERT VERSION (enforce)")
@RequiredPlugins("Minecraft 1.17+")
public class CondIsWhitelisted extends Condition {

	private static final boolean ENFORCE_SUPPORT;

	static {
		ENFORCE_SUPPORT = Skript.methodExists(Bukkit.class, "isWhitelistEnforced");
		Skript.registerCondition(CondIsWhitelisted.class,
			"[the] server (is|1¦is(n't| not)) white[ ]listed",
			"%offlineplayers% (is|are)(|1¦(n't| not)) white[ ]listed",
			(ENFORCE_SUPPORT ? "[the] server (is|1¦is(n't| not)) white[ ]list enforced" : ""));
	}

	@Nullable
	private Expression<OfflinePlayer> player;

	private boolean isServer;
	private boolean isEnforce;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setNegated(parseResult.mark == 1);
		if (matchedPattern == 0 || matchedPattern == 2) {
			isServer = true;
			isEnforce = matchedPattern == 2;
		}
		else if (matchedPattern == 1)
			player = (Expression<OfflinePlayer>) exprs[0];
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (isServer)
			return (isEnforce ? Bukkit.isWhitelistEnforced() : Bukkit.hasWhitelist()) ^ isNegated();

		return player.check(event, OfflinePlayer::isWhitelisted, isNegated());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (isServer ? "server" : "player") + " is " + (isNegated() ? "not" : "") + " "
			+ (isEnforce ? "whitelist enforced" : "whitelisted");
	}

}
