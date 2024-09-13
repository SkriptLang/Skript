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
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.apache.commons.lang.NotImplementedException;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("View Distance")
@Description({
	"The view distance of a player as set by the server. Can be changed.",
	"NOTE: This is the view distance sent by the server to the player.",
	"This has nothing to do with client side view distance settings",
	"NOTE: This may not work on some versions (such as MC 1.14.x).",
	"The return value in this case will be the view distance set in system.properties."
})
@Examples({
	"set view distance of player to 10", "set {_view} to view distance of player",
	"reset view distance of all players", "add 2 to view distance of player"
})
@RequiredPlugins("Paper")
@Since("2.4")
public class ExprPlayerViewDistance extends SimplePropertyExpression<Player, Integer> {

	static {
		if (Skript.methodExists(Player.class, "getViewDistance"))
			register(ExprPlayerViewDistance.class, Integer.class, "view distance[s]", "players");
	}

	@Override
	@Nullable
	public Integer convert(Player player) {
		return getViewDistance(player);
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
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		int distance = delta == null ? 0 : ((Number) delta[0]).intValue();
		for (Player player : getExpr().getArray(event)) {
			int oldDistance = getViewDistance(player);
			switch (mode) {
				case DELETE:
				case SET:
					setViewDistance(player, distance);
					break;
				case ADD:
					setViewDistance(player, oldDistance + distance);
					break;
				case REMOVE:
					setViewDistance(player, oldDistance - distance);
					break;
				case RESET:
					setViewDistance(player, Bukkit.getViewDistance());
				default:
					assert false;
			}
		}
	}

	private static int getViewDistance(Player player) {
		try {
			return player.getViewDistance();
		} catch (NotImplementedException ignore) {
			return Bukkit.getViewDistance();
		}
	}

	private static void setViewDistance(Player player, int distance) {
		try {
			player.setViewDistance(distance);
		} catch (NotImplementedException ignore) {
			Skript.error("'player view distance' is not available on your server version. This is NOT a Skript bug.");
		}
	}

	@Override
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	protected String getPropertyName() {
		return "view distance";
	}

}
