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
import org.bukkit.event.player.PlayerResourcePackStatusEvent.Status;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;

@Name("Last Resource Pack Response")
@Description("Returns the last resource pack response received from a player.")
@Examples("if player's last resource pack response is deny or download fail:")
@Since("2.4")
@RequiredPlugins("Paper 1.9 or newer")
public class ExprLastResourcePackResponse extends SimplePropertyExpression<Player, Status> {

	static {
		if (Skript.methodExists(Player.class, "getResourcePackStatus"))
			register(ExprLastResourcePackResponse.class, Status.class, "[last] resource pack response[s]", "players");
	}

	@Override
	@Nullable
	public Status convert(final Player p) {
		return p.getResourcePackStatus();
	}

	@Override
	protected String getPropertyName() {
		return "resource pack response";
	}

	@Override
	public Class<Status> getReturnType() {
		return Status.class;
	}

}
