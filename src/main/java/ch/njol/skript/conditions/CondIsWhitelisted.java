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
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Is Whitelisted")
@Description("Whether or not the server or a player is whitelisted.")
@Examples({"if server is whitelisted:", "if player is whitelisted"})
@Since("2.5.2")
public class CondIsWhitelisted extends Condition {
	
	static {
		Skript.registerCondition(CondIsWhitelisted.class,
			"[the] server (is|1¦is(n't| not)) white[ ]listed",
			"%players% (is|are)(|1¦(n't| not)) white[ ]listed");
	}
	
	@Nullable
	private Expression<Player> player;
	
	private boolean isServer;
	
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setNegated(parseResult.mark == 1);
		isServer = matchedPattern == 0;
		if (matchedPattern == 1)
			player = (Expression<Player>) exprs[0];
		return true;
	}
	
	@Override
	@SuppressWarnings("null")
	public boolean check(Event e) {
		if (isServer)
			return Bukkit.hasWhitelist() == isNegated();
		Player[] players = player.getAll(e);
		if (player.getAnd() && isNegated()) {
			for (Player player : players)
				if (player.isWhitelisted())
					return false;
		} else if(player.getAnd()){
			for (Player player : players)
				if (!player.isWhitelisted())
					return false;
		} else {
			for (Player player: players)
				if(player.isWhitelisted())
					return !isNegated();
		}
		return !isNegated();
	}
	
	@Override
	@SuppressWarnings("null")
	public String toString(@Nullable Event e, boolean debug) {
		return (player.getSingle(e) != null ? "player" : "server") + (isNegated() ? "not" : "") + "  whitelisted";
	}
	
}
