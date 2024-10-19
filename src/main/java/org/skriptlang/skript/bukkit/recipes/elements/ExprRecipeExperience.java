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
import ch.njol.skript.util.RecipeUtils.RegisterRecipeEvent.CookingRecipeEvent;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.inventory.CookingRecipe;
import org.bukkit.inventory.Recipe;
import org.jetbrains.annotations.Nullable;

@Name("Recipe Experience")
@Description("The experience of a blasting, furnace, campfire, or smoking recipe.")
@Examples({
	"register a new blasting recipe with the key \"my_recipe\":",
		"\tset the recipe input item to a raw copper named \"Impure Copper\"",
		"\tset the recipe experience to 20",
		"\tset the recipe result to copper ingot named \"Pure Copper\""
})
@Since("INSERT VERSION")
public class ExprRecipeExperience extends PropertyExpression<Recipe, Float> {

	static {
		Skript.registerExpression(ExprRecipeExperience.class, Float.class, ExpressionType.PROPERTY,
			"[the] recipe [e]xp[erience] [of %-recipes%]");
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
			if (!getParser().isCurrentEvent(CookingRecipeEvent.class)) {
				Skript.error("This can only be used when registering a blasting, furnace, campfire or smoking recipe.");
				return false;
			}
			isEvent = true;
			setExpr(new EventValueExpression<>(Recipe.class));
		}
		return true;
	}

	@Override
	protected Float @Nullable [] get(Event event, Recipe[] source) {
		if (isEvent)
			return null;

		return get(source, recipe -> {
			if (recipe instanceof CookingRecipe<?> cookingRecipe) {
				return cookingRecipe.getExperience();
			}
			return null;
		});
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET && isEvent)
			return CollectionUtils.array(Float.class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (!(event instanceof CookingRecipeEvent cookingEvent))
			return;

		float experience = (float) delta[0];
		cookingEvent.setExperience(experience);
	}

	@Override
	public Class<Float> getReturnType() {
		return Float.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the recipe experience of " + getExpr().toString(event, debug);
	}

}
