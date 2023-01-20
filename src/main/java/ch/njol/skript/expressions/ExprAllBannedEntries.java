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

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("All Banned Players/IPs")
@Description("Obtains the list of all banned players or IP addresses.")
@Examples({
	"command /banlist:",
	"\ttrigger:",
	"\t\tsend all the banned players"
})
@Since("INSERT VERSION")
public class ExprAllBannedEntries extends SimpleExpression<Object> {

	static {
		Skript.registerExpression(ExprAllBannedEntries.class, Object.class, ExpressionType.SIMPLE, "[all [[of] the]|the] banned (players|ips:(ips|ip addresses))");
	}

	private boolean ip;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		ip = parseResult.hasTag("ips");
		return true;
	}

	@Override
	@Nullable
	protected Object[] get(Event event) {
		if (ip)
			return Bukkit.getIPBans().toArray(new String[0]);
		return Bukkit.getBannedPlayers().toArray(new OfflinePlayer[0]);
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<?> getReturnType() {
		return ip ? String.class : OfflinePlayer.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "all banned " + (ip ? "ip addresses" : "players");
	}

}
