package org.skriptlang.skript.bukkit.recipes.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.Recipe;
import org.jetbrains.annotations.Nullable;

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
			"make %players% (discover|unlock) [the] [recipe[s]] %recipes%",
			"make %players% forget [the] [recipe[s]] %recipes%",
			"(discover|unlock) [the] [recipe[s]] %recipes% for %players%",
			"(undiscover|lock) [the] [recipe[s]] %recipes% for %players%");
	}

	private Expression<Player> players;
	private Expression<? extends Recipe> recipes;
	private boolean isDiscover = false;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		isDiscover = matchedPattern == 0 || matchedPattern == 2;
		//noinspection unchecked
		players = (Expression<Player>) (matchedPattern <= 1 ? exprs[0] : exprs[1]);
		//noinspection unchecked
		recipes = (Expression<? extends Recipe>) (matchedPattern <= 1 ? exprs[1] : exprs[0]);
		return true;
	}

	@Override
	protected void execute(Event event) {
		Player[] playerArray = players.getArray(event);
		for (Object object : recipes.getArray(event)) {
			if (object instanceof Recipe actualRecipe && actualRecipe instanceof Keyed recipeKey) {
				NamespacedKey key = recipeKey.getKey();
				for (Player player : playerArray) {
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
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append("make", players);
		if (isDiscover) {
			builder.append("discover");
		} else {
			builder.append("undiscover");
		}
		builder.append("recipes", recipes);
		return builder.toString();
	}

}
