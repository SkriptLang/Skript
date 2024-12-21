package org.skriptlang.skript.bukkit.recipes.elements;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.Keyed;
import org.bukkit.inventory.Recipe;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.recipes.MutableRecipe;

@Name("Recipe Name")
@Description("Get the namespaced key of a recipe.")
@Examples({
	"loop all of the server's recipes:",
		"\tbroadcast the recipe key of loop-recipe",
		"\tadd loop-recipe's id to {_list::*}"
})
@Since("INSERT VERSION")
public class ExprRecipeKey extends SimplePropertyExpression<Recipe, String> {

	static {
		register(ExprRecipeKey.class, String.class, "recipe (key|id)[s]", "recipes");
	}

	@Override
	public @Nullable String convert(Recipe recipe) {
		if (recipe instanceof MutableRecipe recipeWrapper) {
			return recipeWrapper.getKey().toString();
		} else if (recipe instanceof Keyed key) {
			return key.getKey().toString();
		}
		return null;
	}

	@Override
	protected String getPropertyName() {
		return "recipe key";
	}

	@Override
	public Class<String> getReturnType() {
		return String.class;
	}

}
