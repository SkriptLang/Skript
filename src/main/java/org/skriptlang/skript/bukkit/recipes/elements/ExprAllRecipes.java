package org.skriptlang.skript.bukkit.recipes.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import ch.njol.util.coll.iterator.CheckedIterator;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.recipes.RecipeUtils.RecipeType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Name("All Recipes")
@Description({
	"Retrieve all recipes registered in the server. Includes the options to:",
	"<ul>",
	"<li>Get only recipes provided by Minecraft or custom recipes</li>",
	"<li>Specific recipe types</li>",
	"<li>For specific items</li>",
	"</ul>",
	"",
	"When resetting the server recipes, all custom recipes from any plugin will be removed, regardless of specifying additional data. "
		+ "Only vanilla recipes will remain.",
	"When deleting the server recipes, you are allowed to delete recipes using the options listed above.",
})
@Examples({
	"set {_list::*} to all of the server's recipe of type shaped recipe for netherite ingot",
	"set {_list::*} to the server's mc recipes of type cooking recipe for raw beef",
	"set {_list::*} to server's custom recipes of type blasting for raw iron named \"Impure Iron\"",
	"",
	"reset all of the server's recipes",
	"",
	"delete all the server's recipes for netherite ingot",
	"clear all of the server minecraft recipes",
	"delete all of the server's custom shaped recipes"
})
@Since("INSERT VERSION")
public class ExprAllRecipes extends SimpleExpression<Recipe> {

	static {
		Skript.registerExpression(ExprAllRecipes.class, Recipe.class, ExpressionType.SIMPLE,
			"[all [[of] the]|the] server['s] recipes [for %-itemstacks/itemtypes%]",
			"[all [[of] the]|the] server['s] (mc|minecraft|vanilla) recipes [for %-itemstacks/itemtypes%]",
			"[all [[of] the]|the] server['s] custom recipes [for %-itemstacks/itemtypes%]",
			"[all [[of] the]|the] server['s] %recipetype% [for %-itemstacks/itemtypes%]",
			"[all [[of] the]|the] server['s] (mc|minecraft|vanilla) %recipetype% [for %-itemstacks/itemtypes%]",
			"[all [[of] the]|the] server['s] custom %recipetype% [for %-itemstacks/itemtypes%]");
	}

	private Expression<RecipeType> recipeTypeExpr;
	private Expression<?> itemExpr;
	private boolean getMinecraft, getCustom;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (matchedPattern <= 2 && exprs[0] != null) {
			itemExpr = exprs[0];
		} else if (matchedPattern >= 3) {
			//noinspection unchecked
			recipeTypeExpr = (Expression<RecipeType>) exprs[0];
			itemExpr = exprs[1];
		}
		getMinecraft = matchedPattern == 1 || matchedPattern == 4;
		getCustom = matchedPattern == 2 || matchedPattern == 5;
		return true;
	}

	@Override
	protected Recipe @Nullable [] get(Event event) {
		List<Recipe> recipes = new ArrayList<>();
		CheckedIterator<Recipe> iterator = getSelectedRecipes(event);
		if (iterator == null)
			return null;
		iterator.forEachRemaining(recipes::add);
		return recipes.toArray(new Recipe[0]);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.RESET || mode == ChangeMode.DELETE)
			return CollectionUtils.array(Recipe.class);
		else if (mode == ChangeMode.ADD)
			return CollectionUtils.array(Recipe[].class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		switch (mode) {
			case RESET -> {
				Bukkit.resetRecipes();
			}
			case DELETE -> {
				CheckedIterator<Recipe> iterator = getSelectedRecipes(event);
				if (iterator == null)
					return;
				iterator.forEachRemaining(recipe -> {
					if (recipe instanceof Keyed key)
						Bukkit.removeRecipe(key.getKey());
				});
			}
			case ADD -> {
				Recipe[] recipes = delta != null ? (Recipe[]) delta : null;
				if (recipes == null || recipes.length == 0)
					return;
				for (Recipe recipe : recipes) {
					if (!(recipe instanceof Keyed keyed))
						continue;
					NamespacedKey key = keyed.getKey();
					if (Bukkit.getRecipe(key) != null)
						Bukkit.removeRecipe(key);
					Bukkit.addRecipe(recipe);
				}

			}
		}
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<Recipe> getReturnType() {
		return Recipe.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append("all of the");
		if (getMinecraft) {
			builder.append("minecraft");
		} else if (getCustom) {
			builder.append("custom");
		}
		if (recipeTypeExpr != null)
			builder.append(recipeTypeExpr);
		builder.append("recipes");
		if (itemExpr != null)
			builder.append("for", itemExpr);
		return builder.toString();
	}

	// Gets the recipes based on the provided input from the user
	private CheckedIterator<Recipe> getSelectedRecipes(Event event) {
		Iterator<Recipe> iterator = null;
		// User is looking for a certain type of item(s)
		if (itemExpr != null) {
			List<Recipe> itemRecipes = new ArrayList<>();
			for (Object object : itemExpr.getArray(event)) {
				ItemStack stack = null;
				if (object instanceof ItemStack itemStack) {
					stack = itemStack;
				} else if (object instanceof ItemType itemType) {
					stack = itemType.getRandom();
				}
				if (stack != null)
					itemRecipes.addAll(Bukkit.getRecipesFor(stack));
			}
			// If no recipes contain the items the user is looking for, return null
			if (itemRecipes.isEmpty())
				return null;
			iterator = itemRecipes.iterator();
		} else {
			// Items not specified, getting all recipes
			iterator = Bukkit.recipeIterator();
		}

		RecipeType recipeType = recipeTypeExpr != null ? recipeTypeExpr.getSingle(event) : null;

		return new CheckedIterator<Recipe>(iterator, recipe -> {
			if (recipe instanceof Keyed keyed) {
				NamespacedKey key = keyed.getKey();
				// If the user wants only "minecraft:" recipes
				if (getMinecraft && !key.getNamespace().equalsIgnoreCase("minecraft"))
					return false;
				// If the user wants only custom recipes
				else if (getCustom && key.getNamespace().equalsIgnoreCase("minecraft"))
					return false;

				// If the user wants a specific recipe type
				if (recipeType != null) {
					if (!(recipe.getClass().equals(recipeType.getRecipeClass()) || recipeType.getRecipeClass().isInstance(recipe)))
						return false;
				}
				return true;
			}
			return false;
		});
	}

}
