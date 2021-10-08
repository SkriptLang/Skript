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
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Make Drop")
@Description("Forces a player to drop only one or all of their held item.")
@Examples({"make player drop their held item", "force all players to drop all of their tool"})
@RequiredPlugins({"Minecraft 1.16+"})
@Since("INSERT VERSION")
public class EffMakeDrop extends Effect {

	static {
		if (Skript.methodExists(HumanEntity.class, "dropItem", boolean.class)) {
			Skript.registerEffect(EffMakeDrop.class, "make %players% drop [(one|1¦all) of] [their] (tool|held item)",
				"force %players% to drop [(one|1¦all) of] [their] (tool|held item)");
		}
	}

	@SuppressWarnings("null")
	private Expression<Player> players;
	private boolean isAll;

	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		players = (Expression<Player>) exprs[0];
		isAll = parseResult.mark == 1;
		return true;
	}

	@Override
	protected void execute(Event e) {
		for (Player p : players.getArray(e)) {
			p.dropItem(isAll);
		}
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "make " + players.toString(e, debug) + " drop " + (isAll ? "all" : "1") + " of their tool";
	}

}
