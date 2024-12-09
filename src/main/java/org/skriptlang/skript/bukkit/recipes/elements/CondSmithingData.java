package org.skriptlang.skript.bukkit.recipes.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.SmithingRecipe;
import org.jetbrains.annotations.Nullable;

@Name("Recipe Allows Data Component Copy")
@Description({
	"Checks whether the recipe allows data component copying from the base item to the result item of a recipe.",
	"NOTE: This condition only works on Smithing, Smithing Transform, and Smithing Trim Recipes."
})
@Examples({
	"loop the server's recipes:",
		"\tif all:",
			"\t\tthe recipe type of loop-value is a smithing recipe",
			"\t\tthe loop-value allows data component copying",
	"then:",
	"broadcast loop-value"
})
@RequiredPlugins("Paper 1.20.5+")
@Since("INSERT VERSION")

public class CondSmithingData extends PropertyCondition<Recipe> {

	private static final boolean SUPPORTS_COPY_DATA = Skript.methodExists(SmithingRecipe.class, "willCopyDataComponents");

	static {
		if (SUPPORTS_COPY_DATA) {
			Skript.registerCondition(CondSmithingData.class, ConditionType.PROPERTY,
				"[the] %recipes% allow[s] data component[s] copying",
				"[the] %recipes% (don't|do not|doesn't|does not) allow data component[s] copying");
		}
	}

	private Expression<Recipe> exprRecipe;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		exprRecipe = (Expression<Recipe>) exprs[0];
		setNegated(matchedPattern == 1);
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public boolean check(Recipe recipe) {
		if (!(recipe instanceof SmithingRecipe smithingRecipe))
			return isNegated();
		return smithingRecipe.willCopyDataComponents();
	}

	@Override
	protected String getPropertyName() {
		return null;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return exprRecipe.toString(event, debug) + (isNegated() ? "does not" : "") + " allow data component copying";
	}

}
