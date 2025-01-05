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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.recipes.CreateRecipeEvent;
import org.skriptlang.skript.bukkit.recipes.MutableRecipe;

@Name("Recipe Result")
@Description("The result item for a recipe.")
@Examples({
	"set {_recipe} to a new shaped recipe with the key \"my_recipe\":",
		"\tset the recipe ingredients of 1st row to diamond, air and diamond",
		"\tset the recipe result to diamond sword named \"Chosen One\""
})
@Since("INSERT VERSION")
public class ExprRecipeResult extends SimplePropertyExpression<Recipe, ItemStack> {

	static {
		registerDefault(ExprRecipeResult.class, ItemStack.class, "recipe result [item]", "recipes");
	}

	private boolean isEvent = false;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (getParser().isCurrentEvent(CreateRecipeEvent.class))
			isEvent = true;
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public @Nullable ItemStack convert(Recipe recipe) {
		return recipe.getResult();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (!isEvent) {
			Skript.error("You can not set the recipe result of existing recipes.");
		} else if (mode == ChangeMode.SET) {
			return CollectionUtils.array(ItemStack.class);
		}
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (delta == null || !(event instanceof CreateRecipeEvent recipeEvent))
			return;

		MutableRecipe mutableRecipe = recipeEvent.getMutableRecipe();

		ItemStack result = (ItemStack) delta[0];
		mutableRecipe.setResult(result);
	}

	@Override
	public Class<ItemStack> getReturnType() {
		return ItemStack.class;
	}

	@Override
	protected String getPropertyName() {
		return "recipe result item";
	}

}
