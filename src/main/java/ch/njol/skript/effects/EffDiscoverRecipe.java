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
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;


@Name("Discover Recipe")
@Description("Discovers/unlocks recipes")
@Examples({"discover recipe \"dirty-diamond-boots\" for player"})
@Since("INSERT VERSION")
public class EffDiscoverRecipe extends Effect {

	static {
		Skript.registerEffect(EffDiscoverRecipe.class,
			"make %players% (discover|unlock) recipe[s] %strings%",
					"make %players% (undiscover|lock) recipe[s] %strings%",
					"(discover|unlock) recipe[s] %strings% for %players%",
					"(undiscover|lock) recipe[s] %strings% for %players%");
	}

	private Expression<Player> players;
	private Expression<String> recipes;
	private boolean discover;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		discover = matchedPattern % 2 == 0;
		players = (Expression<Player>) exprs[matchedPattern <= 1 ? 0 : 1];
		recipes = (Expression<String>) exprs[matchedPattern <= 1 ? 1 : 0];
		return true;
	}

	@Override
	protected void execute(Event e) {
		for (Player player : players.getArray(e)) {
			for (String recipe : recipes.getArray(e)) {
				if (discover) {
					player.discoverRecipe(getKey(recipe));
				} else {
					player.undiscoverRecipe(getKey(recipe));
				}
			}
		}

	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "continue";
	}

	private NamespacedKey getKey(String recipeKey) {
		NamespacedKey key = NamespacedKey.fromString(recipeKey);
		if (key != null)
			return key;
		return Utils.createNamespacedKey(recipeKey);
	}

}
