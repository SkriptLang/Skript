package org.skriptlang.skript.bukkit.recipes.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.bukkitutil.NamespacedUtils;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerRecipeDiscoverEvent;
import org.jetbrains.annotations.Nullable;

@Name("Player Discover Recipe")
@Description("Called when a player discovers a new recipe.")
@Examples({
	"on player discovered recipe:",
		"\tbroadcast event-recipe",
	"",
	"on discovered recipe of \"my_recipe\":",
		"broadcast event-recipe"
})
@Since("INSERT VERSION")
public class EvtDiscoverRecipe extends SkriptEvent {

	static {
		Skript.registerEvent("Player Discover Recipe", EvtDiscoverRecipe.class, PlayerRecipeDiscoverEvent.class,
			"[player] discover[ed|ing] recipe[s] [of %-strings%]");
	}

	private @Nullable Expression<String> recipes;

	@Override
	public boolean init(Literal<?>[] exprs, int matchedPattern, ParseResult parseResult) {
		//noinspection unchecked
		recipes = (Expression<String>) exprs[0];
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (!(event instanceof PlayerRecipeDiscoverEvent discoverEvent))
			return false;
		if (recipes == null)
			return true;

		return recipes.check(event, recipe -> discoverEvent.getRecipe().equals(NamespacedUtils.getNamespacedKey(recipe)));
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "player discovered recipes" + (recipes == null ? "" : " of " + recipes.toString(event, debug));
	}

}
