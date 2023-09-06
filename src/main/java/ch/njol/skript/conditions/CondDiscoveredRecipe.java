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
package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;


@Name("Has Discovered Recipe")
@Description("Checks whether a player has discovered/unlocked a recipe")
@Examples({"discover recipe \"dirty-diamond-boots\" for player"})
@Since("INSERT VERSION")
public class CondDiscoveredRecipe extends Condition {

	static {
		Skript.registerCondition(CondDiscoveredRecipe.class,
			"%players% (has|have) (discovered|unlocked) recipe[s] %strings%",
			"%players% (hasn't|have not) (discovered|unlocked) recipe[s] %strings%");
	}

	private Expression<Player> players;
	private Expression<String> recipes;

	@Override
	@SuppressWarnings({"unchecked", "null"})
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		players = (Expression<Player>) exprs[0];
		setNegated(matchedPattern == 1);
		return true;
	}

	@Override
	public boolean check(Event event) {
		return players.check(event,
			player -> recipes.check(event,
				recipe -> player.hasDiscoveredRecipe(Utils.getNamespacedKey(recipe))),
			isNegated());
	}

	@Override
	public String toString(@Nullable Event event, final boolean debug) {
		return players.toString(event, debug);
	}

}
