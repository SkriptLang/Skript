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
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Player;

@Name("Ping")
@Description("Pings of players, as Minecraft server knows them. Note that they will almost certainly"
		+ " be different from the ones you'd get from using ICMP echo requests."
		+ " This expression is only supported on some server software (PaperSpigot).")
@Examples({"command /ping <player=%player%>:",
			"\ttrigger:",
			"\t\tsend \"%arg-1%'s ping is %arg-1's ping%\""})
@Since("2.2-dev36")
public class ExprPing extends SimplePropertyExpression<Player, Long> {

	private static final boolean SUPPORTED = Skript.methodExists(Player.Spigot.class, "getPing");

	static {
		PropertyExpression.register(ExprPing.class, Long.class, "ping", "players");
	}

	@Override
	@SuppressWarnings({"unchecked", "null"})
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		if (!SUPPORTED) {
			Skript.error("The ping expression is not supported on this server software.");
			return false;
		}
		setExpr((Expression<Player>) exprs[0]);
		return true;
	}

	@Override
	public Long convert(Player player) {
		return (long) player.spigot().getPing();
	}

	@Override
	public Class<? extends Long> getReturnType() {
		return Long.class;
	}

	@Override
	protected String getPropertyName() {
		return "ping";
	}

}
