package org.skriptlang.skript.bukkit.recipes.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.inventory.*;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.recipes.CreateRecipeEvent;
import org.skriptlang.skript.bukkit.recipes.MutableRecipe;
import org.skriptlang.skript.bukkit.recipes.MutableRecipe.MutableGroupRecipe;

@Name("Recipe Group")
@Description({
	"The recipe group of a shaped, shapeless, blasting, furnace, campfire, smoking, or stonecutting recipe.",
	"Groups recipes together under the provided string."
})
@Examples({
	"set {_recipe} to a new shapeless recipe with the key \"my_recipe\":",
		"\tset the recipe ingredients to 3 diamonds, 3 emeralds and 3 netherite ingots",
		"\tset the recipe group to \"my group\"",
		"\tset the recipe result to nether star"
})
@Since("INSERT VERSION")
public class ExprRecipeGroup extends SimplePropertyExpression<Recipe, String> {

	static {
		registerDefault(ExprRecipeGroup.class, String.class, "recipe group", "recipes");
	}

	private boolean isEvent = false;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (exprs[0].isDefault() && getParser().isCurrentEvent(CreateRecipeEvent.class))
			isEvent = true;
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public @Nullable String convert(Recipe recipe) {
		if (recipe instanceof MutableGroupRecipe mutableGroupRecipe)  {
			return mutableGroupRecipe.getGroup();
		} else {
			// TODO: Combine ShapedRecipe and ShapelessRecipe into CraftingRecipe when minimum version is raised to 1.20.1 or higher.
			if (recipe instanceof ShapedRecipe shapedRecipe) {
				return shapedRecipe.getGroup();
			} else if (recipe instanceof ShapelessRecipe shapelessRecipe) {
				return shapelessRecipe.getGroup();
			} else if (recipe instanceof CookingRecipe<?> cookingRecipe) {
				return cookingRecipe.getGroup();
			} else if (recipe instanceof StonecuttingRecipe stonecuttingRecipe) {
				return stonecuttingRecipe.getGroup();
			}
		}
		return null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (!isEvent) {
			Skript.error("You can not set the recipe group of existing recipes.");
		} else if (mode == ChangeMode.SET) {
			return CollectionUtils.array(String.class);
		}
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (!(event instanceof CreateRecipeEvent recipeEvent))
			return;
		MutableRecipe mutableRecipe = recipeEvent.getMutableRecipe();

		String group = (String) delta[0];
		if (group.isEmpty())
			return;

		if (mutableRecipe instanceof MutableGroupRecipe mutableGroupRecipe)
			mutableGroupRecipe.setGroup(group);
	}

	@Override
	public Class<String> getReturnType() {
		return String.class;
	}

	@Override
	protected String getPropertyName() {
		return "recipe group";
	}

}
