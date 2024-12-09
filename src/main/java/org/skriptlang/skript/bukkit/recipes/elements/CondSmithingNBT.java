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
import org.bukkit.inventory.SmithingTransformRecipe;
import org.bukkit.inventory.SmithingTrimRecipe;
import org.jetbrains.annotations.Nullable;

@Name("Recipe Allows NBT Copy")
@Description({
	"Checks whether the recipe allows nbt copying from the base item to the result item of the recipe.",
	"NOTES:",
	"This condition can only be used with Smithing Recipes on PaperMC version 1.19",
	"This condition can be used with Smithing Transform and Smithing Trim Recipes on PaperMC version 1.20.0 -> 1.20.4",
	"This condition is redundant for PaperMC versions 1.20.5+ due to the removal in favor of copying data components and will always return true"
})
@Examples({
	"loop the server's recipes:",
		"\tif all:",
			"\t\tthe recipe type of loop-value is a smithing recipe",
			"\t\tthe loop-value allows nbt copying",
		"then:",
			"broadcast loop-value"
})
@RequiredPlugins("Paper")
@Since("INSERT VERSION")

public class CondSmithingNBT extends PropertyCondition<Recipe> {

	private static final boolean SUPPORTS_COPY_NBT = Skript.methodExists(SmithingRecipe.class, "willCopyNbt");
	private static final boolean SUPPORTS_COPY_DATA = Skript.methodExists(SmithingRecipe.class, "willCopyDataComponents");
	private static final boolean RUNNING_1_20_4 =  Skript.isRunningMinecraft(1, 20, 4);

	static {
		if (SUPPORTS_COPY_NBT) {
			Skript.registerCondition(CondSmithingNBT.class, ConditionType.PROPERTY,
				"[the] %recipes% allow[s] nbt copying",
				"[the] %recipes% (don't|do not|doesn't|does not) allow nbt copying");
		}
	}

	private Expression<Recipe> exprRecipe;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (SUPPORTS_COPY_DATA) {
			Skript.warning("This condition is redundant for your current Paper MC version and will always return true.");
		}
		//noinspection unchecked
		exprRecipe = (Expression<Recipe>) exprs[0];
		setNegated(matchedPattern == 1);
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public boolean check(Recipe recipe) {
		if (SUPPORTS_COPY_DATA)
			return true;
		if (!(recipe instanceof SmithingRecipe smithingRecipe))
			return isNegated();
		if ((recipe instanceof SmithingTransformRecipe || recipe instanceof SmithingTrimRecipe) && !RUNNING_1_20_4) {
			return isNegated();
		}
		return smithingRecipe.willCopyNbt();
	}

	@Override
	protected String getPropertyName() {
		return null;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return exprRecipe.toString(event, debug) + (isNegated() ? "does not" : "") + " allow nbt copying";
	}

}
