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
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("View Distance")
@Description("The view distance of a player. Can be changed.")
@Examples({"set view distance of player to 10", "set {_view} to view distance of player",
		"reset view distance of all players", "add 2 to view distance of player"})
@RequiredPlugins("Paper 1.9-1.13.2")
@Since("2.4")
public class ExprPlayerViewDistance extends PropertyExpression<Player, Long> {
	
	static {
		// Not supported on 1.14 yet
		if (Skript.methodExists(Player.class, "getViewDistance") && !Skript.isRunningMinecraft(1, 14))
			register(ExprPlayerViewDistance.class, Long.class, "view distance[s]", "players");
	}
	
	@Override
	@SuppressWarnings({"unchecked", "null"})
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		setExpr((Expression<Player>) exprs[0]);
		return true;
	}
	
	@Override
	protected Long[] get(Event e, Player[] source) {
		return get(source, player -> (long) player.getViewDistance());
	}
	
	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		switch (mode) {
			case DELETE:
			case SET:
			case ADD:
			case REMOVE:
			case RESET:
				return CollectionUtils.array(Number.class);
		}
		return null;
	}
	
	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		int distance = delta == null ? 0 : ((Number) delta[0]).intValue();
		switch (mode) {
			case DELETE:
			case SET:
				for (Player player : getExpr().getArray(e))
					player.setViewDistance(distance);
				break;
			case ADD:
				for (Player player : getExpr().getArray(e))
					player.setViewDistance(player.getViewDistance() + distance);
				break;
			case REMOVE:
				for (Player player : getExpr().getArray(e))
					player.setViewDistance(player.getViewDistance() - distance);
				break;
			case RESET:
				for (Player player : getExpr().getArray(e))
					player.setViewDistance(Bukkit.getServer().getViewDistance());
			default:
				assert false;
		}
	}
	
	@Override
	public Class<? extends Long> getReturnType() {
		return Long.class;
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "the view distance of " + getExpr().toString(e, debug);
	}
	
}
