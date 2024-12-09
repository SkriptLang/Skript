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
import org.skriptlang.skript.bukkit.recipes.MutableRecipe.MutableSmithingRecipe;
import org.skriptlang.skript.bukkit.recipes.CreateRecipeEvent;
import org.skriptlang.skript.bukkit.recipes.CreateRecipeEvent.SmithingRecipeEvent;
import org.skriptlang.skript.bukkit.recipes.CreateRecipeEvent.SmithingRecipeEvent.SmithingTransformRecipeEvent;
import org.skriptlang.skript.bukkit.recipes.CreateRecipeEvent.SmithingRecipeEvent.SmithingTrimRecipeEvent;

@Name("Recipe NBT Copy")
@Description({
	"Whether or not the NBT from the base item of the recipe should be copied to the result item of the recipe.",
	"NOTES:",
	"This effect can only be used when creating a new Smithing Recipe on PaperMC version 1.19",
	"This effect can be applied to Smithing Transform and Smithing Trim Recipes on PaperMC version 1.20.0 -> 1.20.4",
	"This effect is redundant for PaperMC versions 1.20.5+ due to the removal in favor of copying data components"
})
@Examples({
	"set {_recipe} to a new smithing transform recipe:",
		"\tset the recipe base item to a raw iron named \"Impure Iron\"",
		"\tset the recipe additional item to an iron nugget named \"Pure Iron\"",
		"\tset the recipe result item to an iron ingot named \"Heavenly Iron\"",
		"\tallow the nbt copying from the base item to the result item"
})
@RequiredPlugins("Paper")
@Since("INSERT VERSION")

public class EffSmithingNBT extends Effect {

	private static final boolean SUPPORTS_COPY_NBT = Skript.methodExists(SmithingRecipe.class, "willCopyNbt");
	private static final boolean SUPPORTS_COPY_DATA = Skript.methodExists(SmithingRecipe.class, "willCopyDataComponents");
	private static final boolean RUNNING_1_20_0 = Skript.isRunningMinecraft(1, 20, 0);


	static {
		if (SUPPORTS_COPY_NBT) {
			Skript.registerEffect(EffSmithingNBT.class,
				"allow [the] nbt (copying|transfer) from [the] base item to [the] result item",
				"disallow [the] nbt (copying|transfer) from [the] base item to [the] result item");
		}
	}

	private boolean copyNBT;

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
		}
		if (SUPPORTS_COPY_DATA) {
			Skript.warning("This effect is redundant for your current Paper MC version and has no effects on the creation of this recipe.");
		}
		copyNBT = matchedPattern == 0;
		return true;
	}

	@Override
	protected void execute(Event event) {
		if (!(event instanceof SmithingRecipeEvent smithingEvent))
			return;

		MutableSmithingRecipe mutableRecipe = (MutableSmithingRecipe) smithingEvent.getMutableRecipe();
		mutableRecipe.setCopyNBT(copyNBT);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (copyNBT ? "allow" : "disallow") + " the nbt copying from the base item to the result item";
	}

}
