package org.skriptlang.skript.bukkit.recipes.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.bukkitutil.NamespacedUtils;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.Recipe;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

@Name("Discover Recipe")
@Description("Makes the specified players discover or forget the recipes.")
@Examples({
	"make player discover recipe \"my_recipe\"",
	"make player undiscover recipe \"my_recipe\"",
	"unlock recipe \"my_recipe\" for all players",
	"lock recipe \"my_recipe\" for all players"
})
@Since("INSERT VERSION")
public class EffDiscoverRecipe extends Effect {

	static {
		Skript.registerEffect(EffDiscoverRecipe.class,
			"make %players% (discover|unlock) recipe[s] [with [the] (key|id)[s]] %strings%",
			"make %players% (discover|unlock) recipe[s] %recipes%",
			"make %players% (undiscover|lock|forget) recipe[s] [with [the] (key|id)[s]] %strings%",
			"make %players% (undiscover|lock|forget) recipe[s] %recipes%",
			"(discover|unlock) recipe[s] [with [the] (key|id)[s]] %strings% for %players%",
			"(discover|unlock) recipe[s] %recipes% for %players%",
			"(undiscover|lock|forget) recipe[s] [with [the] (key|id)[s]] %strings% for %players%",
			"(undiscover|lock|forget) recipe[s] %recipes% for %players%");
	}

	private static final Set<Integer> discoverPatterns = Set.of(0, 1, 4, 5);

	private Expression<Player> players;
	private Expression<?> recipes;
	private boolean isDiscover = false;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		isDiscover = discoverPatterns.contains(matchedPattern);
		//noinspection unchecked
		players = (Expression<Player>) (matchedPattern <= 3 ? exprs[0] : exprs[1]);
		recipes = matchedPattern <= 3 ? exprs[1] : exprs[0];
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (Object object : recipes.getArray(event)) {
			NamespacedKey key = null;
			if (object instanceof String recipeName) {
				key = NamespacedUtils.getNamespacedKey(recipeName, false);
				if (Bukkit.getRecipe(key) == null)
					key = null;

			} else if (object instanceof Recipe actualRecipe && actualRecipe instanceof Keyed recipeKey) {
				key = recipeKey.getKey();
			}
			if (key != null) {
				for (Player player : players.getArray(event)) {
					if (isDiscover) {
						player.discoverRecipe(key);
					} else {
						player.undiscoverRecipe(key);
					}
				}
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "make " + players.toString(event, debug) + (isDiscover ? " discover" : " undiscover") + " recipes " + recipes.toString(event, debug);
	}

}
