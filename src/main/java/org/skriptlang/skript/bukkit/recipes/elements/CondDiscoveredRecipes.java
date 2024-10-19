package org.skriptlang.skript.bukkit.recipes.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
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

@Name("Has Discovered Recipe")
@Description("Checks whether players have discovered a recipe.")
@Examples({
	"if player has discovered recipe \"custom_recipe\":",
		"\tgive player 1 diamond",
	"",
	"if all players have not found recipe \"custom_recipe\":",
		"\tkill all players",
})
@Since("INSERT VERSION")
public class CondDiscoveredRecipes extends Condition {

	static {
		Skript.registerCondition(CondDiscoveredRecipes.class,
			"%players% (has|have) (discovered|unlocked) [the] recipe[s] [with [the] (key|id)[s]] %recipes%",
			"%players% (has|have) (discovered|unlocked) [the] recipe[s] %recipes%",
			"%players% (hasn't|has not|haven't|have not) (discovered|unlocked) [the] recipe[s] [with [the] (key|id)[s]] %strings%",
			"%players% (hasn't|has not|haven't|have not) (discovered|unlocked) [the] recipe[s] %recipes%");
	}

	private Expression<Player> players;
	private Expression<?> recipes;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		players = (Expression<Player>) exprs[0];
		recipes = exprs[1];
		setNegated(matchedPattern >= 2);
		return true;
	}

	@Override
	public boolean check(Event event) {
		return players.check(event,
			player -> recipes.check(event,
				recipe -> {
					if (recipe instanceof String recipeName) {
						NamespacedKey key = NamespacedUtils.getNamespacedKey(recipeName);
						if (Bukkit.getRecipe(key) != null)
							return player.hasDiscoveredRecipe(key);
						return isNegated();
					} else if (recipe instanceof Recipe actualRecipe && actualRecipe instanceof Keyed recipeKey) {
						return player.hasDiscoveredRecipe(recipeKey.getKey());
					}
					return isNegated();
				}
			)
		);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return players.toString(event, debug) + (isNegated() ? " have not" : " have") + " found recipes " + recipes.toString(event, debug);
	}
}
