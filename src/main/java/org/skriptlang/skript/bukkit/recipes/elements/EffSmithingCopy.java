package org.skriptlang.skript.bukkit.recipes.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.inventory.SmithingRecipe;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.recipes.CreateRecipeEvent;
import org.skriptlang.skript.bukkit.recipes.CreateRecipeEvent.SmithingRecipeEvent;
import org.skriptlang.skript.bukkit.recipes.CreateRecipeEvent.SmithingRecipeEvent.SmithingTransformRecipeEvent;
import org.skriptlang.skript.bukkit.recipes.CreateRecipeEvent.SmithingRecipeEvent.SmithingTrimRecipeEvent;
import org.skriptlang.skript.bukkit.recipes.MutableRecipe.MutableSmithingRecipe;

@Name("Allow Data Copy")
@Description({
	"Allow the recipe to copy data from the base item to the result item.",
	"NOTES:",
	"This condition can only be used with Smithing Recipes on PaperMC version 1.19",
	"This condition can be used with Smithing Transform and Smithing Trim Recipes on PaperMC version 1.20.0+",
})
@Examples({
	"set {_recipe} to a new smithing transform recipe:",
		"\tset the recipe base item to a raw iron named \"Impure Iron\"",
		"\tset the recipe additional item to an iron nugget named \"Pure Iron\"",
		"\tset the recipe result item to an iron ingot named \"Heavenly Iron\"",
		"\tallow the item data to be copied from the base item to the result item"
})
@RequiredPlugins("Paper")
@Since("INSERT VERSION")
public class EffSmithingCopy extends Effect {
	private static final boolean SUPPORTS_COPY_NBT = Skript.methodExists(SmithingRecipe.class, "willCopyNbt");
	private static final boolean SUPPORTS_COPY_DATA = Skript.methodExists(SmithingRecipe.class, "willCopyDataComponents");
	private static final boolean RUNNING_1_20_0 = Skript.isRunningMinecraft(1, 20, 0);

	static {
		if (SUPPORTS_COPY_NBT || SUPPORTS_COPY_DATA) {
			Skript.registerEffect(EffSmithingCopy.class,
				"allow [the] [item] data to be (copied|transferred) from [the] base item to [the] result item",
				"allow (copying|transferring) [the] [item] data from [the] base item to [the] result item",
				"disallow [the] [item] data to be (copied|transferred) from [the] base item to [the] result item",
				"disallow (copying|transferring) [the] [item] data from [the] base item to [the] result item");
		}
	}

	private boolean copyData;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(CreateRecipeEvent.class)) {
			Skript.error("This effect can only be used when creating a new recipe.");
			return false;
		} else if (!getParser().isCurrentEvent(SmithingRecipeEvent.class)) {
			if (RUNNING_1_20_0) {
				Skript.error("This effect can only be used when creating a new Smithing, Smithing Transform or Smithing Trim Recipe.");
			} else {
				Skript.error("This effect can only be used when creating a new Smithing Recipe.");
			}
			return false;
		} else if (!RUNNING_1_20_0 && (getParser().isCurrentEvent(SmithingTransformRecipeEvent.class, SmithingTrimRecipeEvent.class))) {
			Skript.error("Your Paper MC version only allows this effect for creating Smithing Recipes and not Smithing Transform or Smithing Trim Recipes.");
			return false;
		}
		copyData = matchedPattern <= 1;
		return true;
	}

	@Override
	protected void execute(Event event) {
		if (!(event instanceof SmithingRecipeEvent smithingEvent))
			return;

		MutableSmithingRecipe mutableRecipe = (MutableSmithingRecipe) smithingEvent.getMutableRecipe();
		mutableRecipe.setCopyData(copyData);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (copyData ? "allow" : "disallow") + " copying the data from the base item to the result item";
	}

}
