package org.skriptlang.skript.bukkit.recipes.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.RecipeUtils;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.inventory.Recipe;
import org.jetbrains.annotations.Nullable;

@Name("Recipe Type")
@Description("Get the recipe type of a recipe.")
@Examples({
	"loop all recipes:",
		"\tbroadcast the recipe type of loop-recipe"
})
@Since("INSERT VERSION")
public class ExprRecipeType extends PropertyExpression<Recipe, RecipeUtils.RecipeType> {

	static {
		Skript.registerExpression(ExprRecipeType.class, RecipeUtils.RecipeType.class, ExpressionType.PROPERTY,
			"[the] recipe type of %recipes%",
			"[the] %recipes%'[s] recipe type");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		setExpr((Expression<Recipe>) exprs[0]);
		return true;
	}

	@Override
	protected RecipeUtils.RecipeType @Nullable [] get(Event event, Recipe[] source) {
		return get(source, recipe -> RecipeUtils.getRecipeTypeFromRecipe(recipe));
	}

	@Override
	public Class<RecipeUtils.RecipeType> getReturnType() {
		return RecipeUtils.RecipeType.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the recipe type of " + getExpr().toString(event, debug);
	}

}
