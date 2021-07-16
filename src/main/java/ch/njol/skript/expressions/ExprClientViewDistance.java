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

import org.bukkit.entity.Player;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;

@Name("View Distance of Client")
@Description("The view distance of the client. Can not be changed. " +
	"This differs from the server side view distance of player as this will retrieve the view distance the player has set on their client.")
@Examples({"set {_clientView} to the client view distance of player", "set view distance of player to client view distance of player"})
@RequiredPlugins("1.13.2+")
@Since("2.5")
public class ExprClientViewDistance extends SimplePropertyExpression<Player, Long> {
	
	static {
		if (Skript.methodExists(Player.class, "getClientViewDistance")) {
			register(ExprClientViewDistance.class, Long.class, "client view distance[s]", "players");
		}
	}
	
	@Nullable
	@Override
	public Long convert(Player player) {
		return (long) player.getClientViewDistance();
	}

	@Override
	public Class<? extends Long> getReturnType() {
		return Long.class;
	}

	@Override
	protected String getPropertyName() {
		return "client view distance";
	}

}
