package org.skriptlang.skript.bukkit.recipes.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.bukkitutil.NamespacedUtils;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Recipe Exists")
@Description("Checks to see if the specified recipe exists.")
@Examples({
	"if the recipe \"my_recipe\" exists:",
		"\tremove the recipe \"my_recipe\" from the server"
})
@Since("INSERT VERSION")
public class CondRecipeExists extends Condition {

	static {
		Skript.registerCondition(CondRecipeExists.class,
			"[the] recipe[s] %strings% [does] exist[s]",
			"[the] recipe[s] %strings% (doesn't|does not) exist[s]");
	}

	private Expression<String> recipes;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		recipes = (Expression<String>) exprs[0];
		setNegated(matchedPattern == 1);
		return true;
	}

	@Override
	public boolean check(Event event) {
		return recipes.check(event, recipe -> Bukkit.getRecipe(NamespacedUtils.getNamespacedKey(recipe, false)) != null, isNegated());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "recipes " + recipes.toString(event, debug) + (isNegated() ? " do not" : "") + "exist";
	}
}
