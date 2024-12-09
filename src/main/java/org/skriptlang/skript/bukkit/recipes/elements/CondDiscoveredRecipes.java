package org.skriptlang.skript.bukkit.recipes.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.Keyed;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.Recipe;
import org.jetbrains.annotations.Nullable;

@Name("Has Discovered Recipe")
@Description("Checks whether the players have discovered a recipe.")
@Examples({
	"if player has discovered recipe \"custom_recipe\":",
		"\tgive player 1 diamond",
	"",
	"if all players have not discovered recipe \"custom_recipe\":",
		"\tkill all players",
})
@Since("INSERT VERSION")
public class CondDiscoveredRecipes extends Condition {

	static {
		Skript.registerCondition(CondDiscoveredRecipes.class,
			"%players% (has|have) (discovered|unlocked) [the] [recipe[s]] %recipes%",
			"%players% (hasn't|has not|haven't|have not) (discovered|unlocked) [the] [recipe[s]] %recipes%");
	}

	private Expression<Player> exprPlayer;
	private Expression<? extends Recipe> exprRecipe;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		exprPlayer = (Expression<Player>) exprs[0];
		//noinspection unchecked
		exprRecipe = (Expression<? extends Recipe>) exprs[1];
		setNegated(matchedPattern == 1);
		return true;
	}

	@Override
	public boolean check(Event event) {
		Recipe[] recipes = exprRecipe.getArray(event);
		return exprPlayer.check(event, player -> {
			return SimpleExpression.check(recipes, recipe -> {
				if (!(recipe instanceof Keyed recipeKey))
					return false;
				return player.hasDiscoveredRecipe(recipeKey.getKey());
			}, isNegated(), exprRecipe.getAnd());
		});
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return exprPlayer.toString(event, debug) + (isNegated() ? " have not" : " have") + " found recipes " + exprRecipe.toString(event, debug);
	}

}
