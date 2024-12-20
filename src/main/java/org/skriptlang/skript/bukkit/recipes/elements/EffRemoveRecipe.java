package org.skriptlang.skript.bukkit.recipes.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.inventory.Recipe;
import org.jetbrains.annotations.Nullable;

@Name("Remove Recipe")
@Description({
	"Remove the specified recipes from the server.",
	"This will cause all players who have discovered the recipe to forget it.",
	"Removing a minecraft recipe is not persistent across server restart."
})
@Examples("remove the recipe \"my_recipe\" from the server")
@Since("INSERT VERSION")
public class EffRemoveRecipe extends Effect {

	static {
		Skript.registerEffect(EffRemoveRecipe.class,
			"(remove|delete) [the] [recipe[s]] %recipes% [from [the] server]");
	}

	private Expression<? extends Recipe> recipes;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		recipes = (Expression<? extends Recipe>) exprs[0];
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (Recipe recipe : recipes.getArray(event)) {
			if (recipe instanceof Keyed recipeKey) {
				NamespacedKey key = recipeKey.getKey();
				if (Bukkit.getRecipe(key) != null)
					Bukkit.removeRecipe(key);
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "remove recipes " + recipes.toString(event, debug) + " from the server";
	}

}
