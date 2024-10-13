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
package ch.njol.skript.expressions;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Date;
import ch.njol.util.Kleenean;

@Name("Last/First Login Time")
@Description("When a player last/first logged in the server. 'last login' requires paper to get the last login, otherwise it will get the last time they were seen on the server.")
@Examples({"command /onlinefor:",
	"\ttrigger:",
	"\t\tsend \"You have been online for %difference between player's last login and now%.\"",
	"\t\tsend \"You first joined the server %difference between player's first login and now% ago.\""})
@Since("2.5")
public class ExprLastLoginTime extends SimplePropertyExpression<OfflinePlayer, Date> {
	
	private static boolean LAST_LOGIN = Skript.methodExists(OfflinePlayer.class, "getLastLogin");
	
	static {
		register(ExprLastLoginTime.class, Date.class, "(1¦last|2¦first) login", "offlineplayers");
	}
	
	private boolean first;
	
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		first = parseResult.mark == 2;
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}
	
	@Nullable
	@Override
	public Date convert(OfflinePlayer player) {
		return new Date(first ? player.getFirstPlayed() : (LAST_LOGIN ? player.getLastLogin() : player.getLastPlayed()));
	}
	
	@Override
	public Class<? extends Date> getReturnType() {
		return Date.class;
	}
	
	@Override
	protected String getPropertyName() {
		return "last login date";
	}
	
}
