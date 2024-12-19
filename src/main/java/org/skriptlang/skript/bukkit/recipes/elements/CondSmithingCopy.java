package org.skriptlang.skript.bukkit.recipes.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.SmithingRecipe;
import org.bukkit.inventory.SmithingTransformRecipe;
import org.bukkit.inventory.SmithingTrimRecipe;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.recipes.MutableRecipe.MutableSmithingRecipe;
import org.skriptlang.skript.bukkit.recipes.MutableRecipe.MutableSmithingRecipe.MutableSmithingTransformRecipe;
import org.skriptlang.skript.bukkit.recipes.MutableRecipe.MutableSmithingRecipe.MutableSmithingTrimRecipe;

@Name("Recipe Allows Data Copy")
@Description({
	"Checks whether the recipe allows copying data from the base item to the result item of the recipe.",
	"NOTES:",
	"This condition can only be used with Smithing Recipes on PaperMC version 1.19",
	"This condition can be used with Smithing Transform and Smithing Trim Recipes on PaperMC version 1.20.0+",
})
@Examples({
	"loop the server's recipes:",
		"\tif all:",
			"\t\tthe recipe type of loop-value is a smithing recipe",
			"\t\tthe loop-value allows data copying",
		"\tthen:",
			"\t\tbroadcast loop-value"
})
@RequiredPlugins("Paper")
@Since("INSERT VERSION")
public class CondSmithingCopy extends Condition {

	private static final boolean SUPPORTS_COPY_NBT = Skript.methodExists(SmithingRecipe.class, "willCopyNbt");
	private static final boolean SUPPORTS_COPY_DATA = Skript.methodExists(SmithingRecipe.class, "willCopyDataComponents");
	private static final boolean RUNNING_1_20_0 =  Skript.isRunningMinecraft(1, 20, 0);

	static {
		if (SUPPORTS_COPY_NBT || SUPPORTS_COPY_DATA) {
			Skript.registerCondition(CondSmithingCopy.class, ConditionType.PROPERTY,
				"[the] %recipes% allow[s] [item] data (copying|transferring)",
				"[the] %recipes% allow[s] [the] [item] data to be (copied|transferred)",
				"[the] %recipes% (don't|do not|doesn't|does not) allow[s] [item] data (copying|transferring)",
				"[the] %recipes% (don't|do not|doesn't|does not) allow[s] [the] [item] data to be (copied|transferred)");
		}
	}

	private Expression<Recipe> exprRecipe;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		exprRecipe = (Expression<Recipe>) exprs[0];
		setNegated(matchedPattern >= 2);
		return true;
	}

	@Override
	public boolean check(Event event) {
		return exprRecipe.check(event, recipe -> {
			if (recipe instanceof MutableSmithingRecipe mutableRecipe) {
				if (!RUNNING_1_20_0 && (mutableRecipe instanceof MutableSmithingTransformRecipe || mutableRecipe instanceof MutableSmithingTrimRecipe)) {
					return isNegated();
				}
				return mutableRecipe.willCopyData();
			} else if (recipe instanceof SmithingRecipe smithingRecipe) {
				if (!RUNNING_1_20_0 && (smithingRecipe instanceof SmithingTransformRecipe || smithingRecipe instanceof SmithingTrimRecipe)) {
					return isNegated();
				}
				if (SUPPORTS_COPY_DATA) {
					return smithingRecipe.willCopyDataComponents();
				} else if (SUPPORTS_COPY_NBT) {
					return smithingRecipe.willCopyNbt();
				}
			}
			return isNegated();
		}, isNegated());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return exprRecipe.toString(event, debug) + (isNegated() ? "does not" : "") + " allow [item] data copying";
	}

}
