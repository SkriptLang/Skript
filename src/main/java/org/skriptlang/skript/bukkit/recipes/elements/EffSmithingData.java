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

@Name("Recipe Data Component Copy")
@Description({
	"Whether or not the data components of the base item should be copied to the result item of the recipe.",
	"NOTE: This effect only works when creating a Smithing, Smithing Transform, or Smithing Trim Recipe."
})
@Examples({
	"set {_recipe} to a new smithing transform recipe:",
		"\tset the recipe base item to a raw iron named \"Impure Iron\"",
		"\tset the recipe additional item to an iron nugget named \"Pure Iron\"",
		"\tset the recipe result item to an iron ingot named \"Heavenly Iron\"",
		"\tallow the data component copying from the base item to the result item"
})
@RequiredPlugins("Paper 1.20.5+")
@Since("INSERT VERSION")

public class EffSmithingData extends Effect {

	private static final boolean SUPPORTS_COPY_DATA = Skript.methodExists(SmithingRecipe.class, "willCopyDataComponents");

	static {
		if (SUPPORTS_COPY_DATA) {
			Skript.registerEffect(EffSmithingData.class,
				"allow [the] data component (copying|transfer) from [the] base item to [the] result item",
				"disallow [the] data component (copying|transfer) from [the] base item to [the] result item");
		}
	}

	private boolean copyData;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(CreateRecipeEvent.class, SmithingRecipeEvent.class)) {
			Skript.error("This effect can only be used when creating a new Smithing, Smithing Transform or Smithing Trim Recipe.");
			return false;
		}
		copyData = matchedPattern == 0;
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
		return (copyData ? "allow" : "disallow") + " the data component copying from the base item to the result item";
	}

}
