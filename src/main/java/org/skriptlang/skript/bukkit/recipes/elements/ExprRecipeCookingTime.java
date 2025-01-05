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
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.inventory.CookingRecipe;
import org.bukkit.inventory.Recipe;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.recipes.CreateRecipeEvent;
import org.skriptlang.skript.bukkit.recipes.MutableRecipe;
import org.skriptlang.skript.bukkit.recipes.MutableRecipe.MutableCookingRecipe;

@Name("Recipe Cooking Time")
@Description("The cooking time of a blasting, furnace, campfire or smoking recipe.")
@Examples({
	"set {_recipe} to a new blasting recipe with the key \"my_recipe\":",
		"\tset the recipe input item to raw gold named \"Impure Gold\"",
		"\tset the recipe cooking time to 10 seconds",
		"\tset the recipe result to gold ingot named \"Pure Gold\""
})
@Since("INSERT VERSION")
public class ExprRecipeCookingTime extends SimplePropertyExpression<Recipe, Timespan> {

	static {
		registerDefault(ExprRecipeCookingTime.class, Timespan.class, "recipe cook[ing] time", "recipes");
	}

	private boolean isEvent = false;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (exprs[0].isDefault() && getParser().isCurrentEvent(CreateRecipeEvent.class))
			isEvent = true;
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public @Nullable Timespan convert(Recipe recipe) {
		if (recipe instanceof MutableCookingRecipe mutableCookingRecipe) {
			return new Timespan(TimePeriod.TICK, mutableCookingRecipe.getCookingTime());
		} else if (recipe instanceof CookingRecipe<?> cookingRecipe) {
			return new Timespan(TimePeriod.TICK, cookingRecipe.getCookingTime());
		}
		return null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (!isEvent) {
			Skript.error("You can not set the recipe cooking time of existing recipes.");
		} else if (mode == ChangeMode.SET) {
			return CollectionUtils.array(Timespan.class);
		}
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (!(event instanceof CreateRecipeEvent recipeEvent))
			return;

		Timespan timespan = (Timespan) delta[0];
		MutableRecipe mutableRecipe = recipeEvent.getMutableRecipe();
		if (!(mutableRecipe instanceof MutableCookingRecipe mutableCookingRecipe))
			return;
		mutableCookingRecipe.setCookingTime((int) timespan.getAs(Timespan.TimePeriod.TICK));
	}

	@Override
	public Class<Timespan> getReturnType() {
		return Timespan.class;
	}

	@Override
	protected String getPropertyName() {
		return "recipe cooking time";
	}

}
