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

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Spectator Target")
@Description({"Returns the entity which is followed by the camera of the player when in spectator mode."})
@Examples({"command /spectate <player>:",
	"\texecutable by: players",
	"\ttrigger:",
	"\t\tset gamemode of player to spectator # otherwise below won't do anything",
	"\t\tset spectator target of player to arg-1",
	"\t\tsend \"Spectating %arg-1%\" to player"})
@Since("2.4")
public class ExprSpectatorTarget extends SimplePropertyExpression<Player, Entity> {

	static {
		register(ExprSpectatorTarget.class, Entity.class, "spectator target", "players");
	}

	@Override
	protected String getPropertyName() {
		return "spectator target";
	}

	@Nullable
	@Override
	public Entity convert(Player player) {
		return player.getSpectatorTarget();
	}

	@Override
	public Class<? extends Entity> getReturnType() {
		return Entity.class;
	}

	@Nullable
	@Override
	public Class<?>[] acceptChange(Changer.ChangeMode mode) {
		if (mode == Changer.ChangeMode.SET
				|| mode == Changer.ChangeMode.RESET
				|| mode == Changer.ChangeMode.DELETE) {
			return CollectionUtils.array(Entity.class);
		}
		return null;
	}

	@Override
	public void change(Event e, @Nullable Object[] delta, Changer.ChangeMode mode) {
		for (Player player : getExpr().getArray(e)) {
			if (player.getGameMode() == GameMode.SPECTATOR) {
				switch (mode) {
					case SET:
						assert delta != null;
						player.setSpectatorTarget((Entity) delta[0]);
						break;
					case RESET:
					case DELETE:
						player.setSpectatorTarget(null);
				}
			}
		}
	}

}
