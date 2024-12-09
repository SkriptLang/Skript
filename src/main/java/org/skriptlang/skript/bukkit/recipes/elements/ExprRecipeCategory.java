package org.skriptlang.skript.bukkit.recipes.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.inventory.CookingRecipe;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.recipe.CookingBookCategory;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.recipes.MutableRecipe;
import org.skriptlang.skript.bukkit.recipes.MutableRecipe.MutableCookingRecipe;
import org.skriptlang.skript.bukkit.recipes.MutableRecipe.MutableCraftingRecipe;
import org.skriptlang.skript.bukkit.recipes.RecipeCategory;
import org.skriptlang.skript.bukkit.recipes.CreateRecipeEvent;

@Name("Recipe Category")
@Description("The recipe category of a shaped, shapeless, blasting, furnace, campfire or smoking recipe.")
@Examples({
	"set {_recipe} to a new shaped recipe with the key \"my_recipe\":",
		"\tset the recipe ingredients to diamond, air, diamond, air, emerald, air, diamond, air and diamond",
		"\tset the recipe category to misc crafting category",
		"\tset the recipe result item to nether star",
	"",
	"set {_recipe} to a new blasting recipe with the id \"my_recipe\":",
		"\tset the recipe input item to coal",
		"\tset the recipe category to misc cooking category",
		"\tset the recipe result item to gunpowder",
	"",
	"loop the server's recipes:",
		"\tbroadcast recipe category of loop-recipe"
})
@Since("INSERT VERSION")
public class ExprRecipeCategory extends PropertyExpression<Recipe, RecipeCategory> {

	static {
		Skript.registerExpression(ExprRecipeCategory.class, RecipeCategory.class, ExpressionType.PROPERTY,
			"[the] recipe category [of %recipes%]",
			"%recipes%'[s] recipe category");
	}

	private boolean isEvent = false;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (exprs[0].isDefault() && getParser().isCurrentEvent(CreateRecipeEvent.class))
			isEvent = true;
		//noinspection unchecked
		setExpr((Expression<? extends Recipe>) exprs[0]);
		return true;
	}

	@Override
	protected RecipeCategory @Nullable [] get(Event event, Recipe[] source) {
		return get(source, recipe -> {
			Enum<?> category = null;
			if (recipe instanceof MutableRecipe mutableRecipe) {
				if (mutableRecipe instanceof MutableCraftingRecipe mutableCraftingRecipe) {
					category = mutableCraftingRecipe.getCategory();
				} else if (mutableRecipe instanceof MutableCookingRecipe mutableCookingRecipe) {
					category = mutableCookingRecipe.getCategory();
				}
			} else {
				if (recipe instanceof ShapedRecipe shapedRecipe) {
					category = shapedRecipe.getCategory();
				} else if (recipe instanceof ShapelessRecipe shapelessRecipe) {
					category = shapelessRecipe.getCategory();
				} else if (recipe instanceof CookingRecipe<?> cookingRecipe) {
					category = cookingRecipe.getCategory();
				}
			}
			if (category != null)
				return RecipeCategory.convertBukkitToSkript(category);
			return null;
		});
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (!isEvent) {
			Skript.error("You can not set the recipe category of existing recipes.");
		} else if (mode == ChangeMode.SET) {
			return CollectionUtils.array(RecipeCategory.class);
		}
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (!(event instanceof CreateRecipeEvent recipeEvent))
			return;
		RecipeCategory recipeCategory = (RecipeCategory) (delta != null ? delta[0] : null);
		MutableRecipe mutableRecipe = recipeEvent.getMutableRecipe();
		if (mutableRecipe instanceof MutableCraftingRecipe mutableCraftingRecipe) {
			if (!(recipeCategory.getCategory() instanceof CraftingBookCategory category))
				return;
			mutableCraftingRecipe.setCategory(category);
		} else if (mutableRecipe instanceof MutableCookingRecipe mutableCookingRecipe) {
			if (!(recipeCategory.getCategory() instanceof CookingBookCategory category))
				return;
			mutableCookingRecipe.setCategory(category);
		}
	}

	@Override
	public Class<RecipeCategory> getReturnType() {
		return RecipeCategory.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the recipe category of " + getExpr().toString(event, debug);
	}
}
