package org.skriptlang.skript.bukkit.recipes.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.skriptlang.skript.bukkit.recipes.RecipeUtils.RegisterRecipeEvent;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.inventory.*;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.recipes.RecipeWrapper;

@Name("Recipe Group")
@Description("The recipe group of a shaped, shapeless, blasting, furnace, campfire or smoking recipe.")
@Examples({
	"register a new shapeless recipe with the key \"my_recipe\":",
		"\tset the recipe ingredients to 3 diamonds, 3 emeralds and 3 netherite ingots",
		"\tset the recipe group to \"my group\"",
		"\tset the recipe result to nether star"
})
@Since("INSERT VERSION")
public class ExprRecipeGroup extends PropertyExpression<Recipe, String> {

	static {
		Skript.registerExpression(ExprRecipeGroup.class, String.class, ExpressionType.PROPERTY, "[the] recipe group [of %recipes%]");
	}

	private boolean isEvent = false;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!exprs[0].isDefault()) {
			//noinspection unchecked
			setExpr((Expression<? extends Recipe>) exprs[0]);
		} else {
			if (exprs[0] == null) {
				Skript.error("There is no recipe in a '" + getParser().getCurrentEventName() + "' event.");
				return false;
			}
			if (getParser().isCurrentEvent(RegisterRecipeEvent.class))
				isEvent = true;
			setExpr(new EventValueExpression<>(Recipe.class));
		}
		return true;
	}

	@Override
	protected String[] get(Event event, Recipe[] source) {
		return get(source, recipe -> {
			if (recipe instanceof RecipeWrapper)  {
				if (recipe instanceof RecipeWrapper.CraftingRecipeWrapper craftingRecipeWrapper) {
					return craftingRecipeWrapper.getGroup();
				} else if (recipe instanceof RecipeWrapper.CookingRecipeWrapper cookingRecipeWrapper) {
					return cookingRecipeWrapper.getGroup();
				}
			} else {
				if (recipe instanceof ShapedRecipe shapedRecipe) {
					return shapedRecipe.getGroup();
				} else if (recipe instanceof ShapelessRecipe shapelessRecipe) {
					return shapelessRecipe.getGroup();
				} else if (recipe instanceof CookingRecipe<?> cookingRecipe) {
					return cookingRecipe.getGroup();
				}
			}
			return null;
		});
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET && isEvent)
			return CollectionUtils.array(String.class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (!(event instanceof RegisterRecipeEvent recipeEvent))
			return;
		RecipeWrapper recipeWrapper = recipeEvent.getRecipeWrapper();

		String group = (String) delta[0];
		if (group.isEmpty())
			return;

		if (recipeWrapper instanceof RecipeWrapper.CraftingRecipeWrapper craftingRecipeWrapper) {
			craftingRecipeWrapper.setGroup(group);
		} else if (recipeWrapper instanceof RecipeWrapper.CookingRecipeWrapper cookingRecipeWrapper) {
			cookingRecipeWrapper.setGroup(group);
		}
	}

	@Override
	public Class<String> getReturnType() {
		return String.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the recipe group of " + getExpr().toString(event, debug);
	}

}
