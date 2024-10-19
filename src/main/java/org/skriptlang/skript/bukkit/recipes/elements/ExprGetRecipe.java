package org.skriptlang.skript.bukkit.recipes.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.bukkitutil.NamespacedUtils;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.inventory.Recipe;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Name("Recipe")
@Description("Returns the recipe registered with the provided name.")
@Examples({
	"set {_recipe} to recipe with the key \"my_recipe\"",
	"set {_recipes::*} to recipes with the ids \"my_recipe\" and \"custom_recipe\""
})
@Since("INSERT VERSION")
public class ExprGetRecipe extends SimpleExpression<Recipe> {

	static {
		Skript.registerExpression(ExprGetRecipe.class, Recipe.class, ExpressionType.SIMPLE,
			"[the] recipe[s] [with [the] (key|id)[s]] %strings%");
	}

	private Expression<String> recipeNames;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		recipeNames = (Expression<String>) exprs[0];
		return true;
	}

	@Override
	protected Recipe @Nullable [] get(Event event) {
		List<Recipe> recipeList = new ArrayList<>();
		for (String name : recipeNames.getArray(event)) {
			NamespacedKey key = NamespacedUtils.getNamespacedKey(name);
			Recipe check = Bukkit.getRecipe(key);
			if (check != null)
				recipeList.add(check);
		}
		return recipeList.toArray(new Recipe[0]);
	}

	@Override
	public boolean isSingle() {
		return recipeNames.isSingle();
	}

	@Override
	public Class<Recipe> getReturnType() {
		return Recipe.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "recipes with the keys " + recipeNames.toString(event, debug);
	}

}
