package org.skriptlang.skript.bukkit.recipes.elements;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.event.Event;
import org.bukkit.inventory.Recipe;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.recipes.RecipeUtils;
import org.skriptlang.skript.bukkit.recipes.RecipeUtils.RecipeType;
import org.skriptlang.skript.bukkit.recipes.MutableRecipe;

@Name("Recipe Type")
@Description("Get the recipe type of a recipe.")
@Examples({
	"loop all of the server's recipes:",
		"\tbroadcast the recipe type of loop-recipe"
})
@Since("INSERT VERSION")
public class ExprRecipeType extends SimplePropertyExpression<Recipe, RecipeType> {

	static {
		register(ExprRecipeType.class, RecipeType.class, "recipe type", "recipes");
	}

	@Override
	public @Nullable RecipeType convert(Recipe recipe) {
		if (recipe instanceof MutableRecipe recipeWrapper)
			return recipeWrapper.getRecipeType();
		return RecipeUtils.getRecipeType(recipe);
	}

	@Override
	protected String getPropertyName() {
		return "recipe type";
	}

	@Override
	public Class<RecipeType> getReturnType() {
		return RecipeType.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the recipe type of " + getExpr().toString(event, debug);
	}

}
