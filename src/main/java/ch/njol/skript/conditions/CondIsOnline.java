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

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@Name("Is Online")
@Description("Checks whether a player is online.")
@Examples({"player is online",
		"player-argument is offline"})
@Since("1.4")
public class CondIsOnline extends PropertyCondition<OfflinePlayer> {
	
	static {
		register(CondIsOnline.class, "(online|1¦offline)", "offlineplayers");
	}
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		setExpr((Expression<OfflinePlayer>) exprs[0]);
		setNegated(matchedPattern == 1 ^ parseResult.mark == 1);
		return true;
	}
	
	@Override
	public boolean check(OfflinePlayer op) {
		if (Bukkit.getPlayerExact(op.getName()) != null) {return true;}
	}
	
	@Override
	protected String getPropertyName() {
		return "online";
	}
	
}
