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
import org.bukkit.inventory.*;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.recipes.MutableRecipe;
import org.skriptlang.skript.bukkit.recipes.MutableRecipe.MutableCookingRecipe;
import org.skriptlang.skript.bukkit.recipes.MutableRecipe.MutableCraftingRecipe;
import org.skriptlang.skript.bukkit.recipes.MutableRecipe.MutableStonecuttingRecipe;
import org.skriptlang.skript.bukkit.recipes.RegisterRecipeEvent;

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
public class ExprRecipeGroup extends PropertyExpression<Recipe, String> {

	static {
		Skript.registerExpression(ExprRecipeGroup.class, String.class, ExpressionType.PROPERTY,
			"[the] recipe group [of %recipes%]",
			"%recipes%'[s] recipe group");
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
	protected String[] get(Event event, Recipe[] source) {
		return get(source, recipe -> {
			if (recipe instanceof MutableRecipe)  {
				if (recipe instanceof MutableCraftingRecipe mutableCraftingRecipe) {
					return mutableCraftingRecipe.getGroup();
				} else if (recipe instanceof MutableCookingRecipe mutableCookingRecipe) {
					return mutableCookingRecipe.getGroup();
				} else if (recipe instanceof MutableStonecuttingRecipe mutableStonecuttingRecipe) {
					return mutableStonecuttingRecipe.getGroup();
				}
			} else {
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
		});
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (!isEvent) {
			Skript.error("You can not set the recipe group of existing recipes.");
		} else {
			if (mode == ChangeMode.SET) {
				return CollectionUtils.array(String.class);
			}
		}
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (!(event instanceof RegisterRecipeEvent recipeEvent))
			return;
		MutableRecipe mutableRecipe = recipeEvent.getMutableRecipe();

		String group = (String) delta[0];
		if (group.isEmpty())
			return;

		if (mutableRecipe instanceof MutableCraftingRecipe mutableCraftingRecipe) {
			mutableCraftingRecipe.setGroup(group);
		} else if (mutableRecipe instanceof MutableCookingRecipe mutableCookingRecipe) {
			mutableCookingRecipe.setGroup(group);
		} else if (mutableRecipe instanceof MutableStonecuttingRecipe mutableStonecuttingRecipe) {
			mutableStonecuttingRecipe.setGroup(group);
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
