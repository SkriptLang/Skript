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
	"set {_recipe} to a new blasting recipe with the key \"my_recipe\":",
		"\tset the recipe input item to raw gold named \"Impure Gold\"",
		"\tset the recipe cooking time to 10 seconds",
		"\tset the recipe result to gold ingot named \"Pure Gold\""
})
@Since("INSERT VERSION")
public class ExprRecipeCookingTime extends PropertyExpression<Recipe, Timespan> {

	static {
		Skript.registerExpression(ExprRecipeCookingTime.class, Timespan.class, ExpressionType.PROPERTY,
			"[the] recipe cook[ing] time [of %recipes%]",
			"%recipes%'[s] recipe cook[ing] time");
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
	protected Timespan @Nullable [] get(Event event, Recipe[] source) {
		return get(source, recipe -> {
			if (recipe instanceof MutableCookingRecipe mutableCookingRecipe) {
				return new Timespan(Timespan.TimePeriod.TICK, mutableCookingRecipe.getCookingTime());
			} else if (recipe instanceof CookingRecipe<?> cookingRecipe) {
				return new Timespan(Timespan.TimePeriod.TICK, cookingRecipe.getCookingTime());
			}
			return null;
		});
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (!isEvent) {
			Skript.error("You can not set the recipe cooking time of existing recipes.");
		} else {
			if (mode == ChangeMode.SET) {
				return CollectionUtils.array(Timespan.class);
			}
		}
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (!(event instanceof RegisterRecipeEvent recipeEvent))
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
	public String toString(@Nullable Event event, boolean debug) {
		return "the recipe cooking time of " + getExpr().toString(event, debug);
	}

}
