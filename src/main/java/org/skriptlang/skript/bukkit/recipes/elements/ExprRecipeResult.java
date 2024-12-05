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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.recipes.MutableRecipe;
import org.skriptlang.skript.bukkit.recipes.RegisterRecipeEvent;

@Name("Recipe Result")
@Description("The result item for a recipe.")
@Examples({
	"set {_recipe} to a new shaped recipe with the key \"my_recipe\":",
		"\tset the recipe ingredients of 1st row to diamond, air and diamond",
		"\tset the recipe result to diamond sword named \"Chosen One\""
})
@Since("INSERT VERSION")
public class ExprRecipeResult extends PropertyExpression<Recipe, ItemStack> {

	static {
		Skript.registerExpression(ExprRecipeResult.class, ItemStack.class, ExpressionType.PROPERTY,
			"[the] recipe result[ing] [item] [of %recipes%]",
			"%recipes%'[s] recipe result[ing] [item]");
	}

	private boolean isEvent = false;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (exprs[0].isDefault()) {
			if (exprs[0] == null) {
				Skript.error("There is no recipe in a '" + getParser().getCurrentEventName() + "' event.");
				return false;
			}
			if (getParser().isCurrentEvent(RegisterRecipeEvent.class))
				isEvent = true;
		}
		//noinspection unchecked
		setExpr((Expression<? extends Recipe>) exprs[0]);
		return true;
	}

	@Override
	protected ItemStack @Nullable [] get(Event event, Recipe[] source) {
		return get(source, recipe -> {
			if (recipe instanceof MutableRecipe mutableRecipe)
				return mutableRecipe.getResult();
			return recipe.getResult();
		});
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (!isEvent) {
			Skript.error("You can not set the recipe result of existing recipes.");
		} else {
			if (mode == ChangeMode.SET) {
				return CollectionUtils.array(ItemStack.class);
			}
		}
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (!(event instanceof RegisterRecipeEvent recipeEvent))
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
	public String toString(@Nullable Event event, boolean debug) {
		return "the recipe result item of " + getExpr().toString(event, debug);
	}

}
