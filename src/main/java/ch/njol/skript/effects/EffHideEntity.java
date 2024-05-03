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
package ch.njol.skript.effects;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Hide Entities From Players")
@Description({
	"Hide entities from players",
	"Do note that hidden entities still exist in the server space and can interact with the world."
})
@Examples({
	"set {_player} to player",
	"spawn a block display at location infront of player:",
		"\thide event-entity from all players where [player input is not {_player}]"
})
@RequiredPlugins("Spigot 1.19.4+")
@Since("INSERT VERSION")
public class EffHideEntity extends Effect {

	static {
		if (Skript.isRunningMinecraft(1, 19, 4))
			Skript.registerEffect(EffHideEntity.class,
					"hide %entities% from %players%",
					"show %entities% to %players%");
	}

	private Expression<Entity> entities;
	private Expression<Player> players;
	boolean hide;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		entities = (Expression<Entity>) exprs[0];
		players = (Expression<Player>) exprs[1];
		hide = matchedPattern == 0;
		return true;
	}

	@Override
	protected void execute(Event event) {
		Skript skript = Skript.getInstance();
		if (hide) {
			for (Player player : players.getArray(event)) {
				for (Entity entity : entities.getArray(event))
					player.hideEntity(skript, entity);
			}
		} else {
			for (Player player : players.getArray(event)) {
				for (Entity entity : entities.getArray(event))
					player.showEntity(skript, entity);
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (hide ? "hide " : "show ") + entities.toString(event, debug) + (hide ? " from " : " to " + players.toString(event, debug));
	}

}
