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
import ch.njol.skript.util.RecipeUtils.RegisterRecipeEvent;
import ch.njol.skript.util.RecipeUtils.RegisterRecipeEvent.*;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.inventory.*;
import org.jetbrains.annotations.Nullable;

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

	private static final boolean SUPPORT_CRAFTING_TYPE = Skript.classExists("org.bukkit.inventory.CraftingRecipe");

	static {
		Skript.registerExpression(ExprRecipeGroup.class, String.class, ExpressionType.PROPERTY, "[the] recipe group [of %-recipes%]");
	}

	private boolean isEvent = false;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (exprs[0] != null) {
			setExpr((Expression<? extends Recipe>) exprs[0]);
		} else {
			if (!getParser().isCurrentEvent(RegisterRecipeEvent.class)) {
				Skript.error("There is no 'recipe' in a " + getParser().getCurrentEventName() + " event.");
				return false;
			}
			if (!getParser().isCurrentEvent(CraftingRecipeEvent.class, CookingRecipeEvent.class)) {
				Skript.error("This can only be used when registering a Shaped, Shapeless, Cooking, Blasting, Furnace, Campfire or Smoking Recipe.");
				return false;
			}
			isEvent = true;
			setExpr(new EventValueExpression<>(Recipe.class));
		}
		return true;
	}

	@Override
	protected String[] get(Event event, Recipe[] source) {
		if (isEvent)
			return null;

		return get(source, recipe -> {
			if (recipe instanceof CookingRecipe<?> cookingRecipe) {
				return cookingRecipe.getGroup();
			} else if (SUPPORT_CRAFTING_TYPE && recipe instanceof CraftingRecipe craftingRecipe) {
				return craftingRecipe.getGroup();
			} else if (recipe instanceof ShapedRecipe shapedRecipe) {
				return shapedRecipe.getGroup();
			} else if (recipe instanceof ShapelessRecipe shapelessRecipe) {
				return shapelessRecipe.getGroup();
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
		String group = (String) delta[0];
		if (group.isEmpty())
			return;

		if (event instanceof CookingRecipeEvent cookingEvent) {
			cookingEvent.setGroup(group);
		} else if (event instanceof CraftingRecipeEvent craftingEvent) {
			craftingEvent.setGroup(group);
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
