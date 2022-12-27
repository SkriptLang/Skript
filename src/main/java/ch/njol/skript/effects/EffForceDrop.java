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
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Force Drop Item")
@Description("Force a player to drop 1 or all of the items in their main hand.")
@Examples({"on step on dirt:",
	"\tforce player to drop all items in hand"})
@RequiredPlugins("Minecraft 1.16+")
@Since("INSERT VERSION")
public class EffForceDrop extends Effect {

	static {
		if (Skript.methodExists(HumanEntity.class, "dropItem", boolean.class)) {
			Skript.registerEffect(EffForceDrop.class,
				"force %players% to drop [(all|1¦(one|1)) [of]] item[s] in hand");
		}
	}

	private Expression<Player> players;
	private boolean dropAll;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		players = (Expression<Player>) exprs[0];
		dropAll = parseResult.mark == 0;
		return true;
	}

	@Override
	protected void execute(Event e) {
		for (Player player : players.getArray(e)) {
			player.dropItem(dropAll);
		}
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		String all = dropAll ? "all of the items" : "one of the items";
		return String.format("force %s to drop %s in hand", players.toString(e, debug), all);
	}

}
