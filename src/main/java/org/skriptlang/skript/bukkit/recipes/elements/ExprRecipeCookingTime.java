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
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.inventory.CookingRecipe;
import org.bukkit.inventory.Recipe;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.recipes.MutableRecipe;
import org.skriptlang.skript.bukkit.recipes.MutableRecipe.MutableCookingRecipe;
import org.skriptlang.skript.bukkit.recipes.RegisterRecipeEvent;

@Name("Recipe Cooking Time")
@Description("The cooking time of a blasting, furnace, campfire or smoking recipe.")
@Examples({
	"register a new blasting recipe with the key \"my_recipe\":",
		"\tset the recipe input item to raw gold named \"Impure Gold\"",
		"\tset the recipe cooking time to 10 seconds",
		"\tset the recipe result to gold ingot named \"Pure Gold\""
})
@Since("INSERT VERSION")
public class ExprRecipeCookingTime extends PropertyExpression<Recipe, Timespan> {

	static {
		Skript.registerExpression(ExprRecipeCookingTime.class, Timespan.class, ExpressionType.PROPERTY,
			"[the] recipe cook[ing] time [of %recipes%]");
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
	protected Timespan @Nullable [] get(Event event, Recipe[] source) {
		return get(source, recipe -> {
			if (recipe instanceof MutableCookingRecipe cookingRecipeWrapper) {
				return new Timespan(Timespan.TimePeriod.TICK, cookingRecipeWrapper.getCookingTime());
			} else if (recipe instanceof CookingRecipe<?> cookingRecipe) {
				return new Timespan(Timespan.TimePeriod.TICK, cookingRecipe.getCookingTime());
			}
			return null;
		});
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET && isEvent)
			return CollectionUtils.array(Timespan.class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (!(event instanceof RegisterRecipeEvent recipeEvent))
			return;

		Timespan timespan = (Timespan) delta[0];
		MutableRecipe recipeWrapper = recipeEvent.getRecipeWrapper();
		if (!(recipeWrapper instanceof MutableCookingRecipe cookingRecipeWrapper))
			return;
		cookingRecipeWrapper.setCookingTime((int) timespan.getAs(Timespan.TimePeriod.TICK));
	}

	@Override
	public Class<Timespan> getReturnType() {
		return Timespan.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the recipe cooking time of " + getExpr().toString(event, debug);
	}

}
