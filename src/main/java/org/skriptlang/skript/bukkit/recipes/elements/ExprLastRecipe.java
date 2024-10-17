package org.skriptlang.skript.bukkit.recipes.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.inventory.Recipe;
import org.jetbrains.annotations.Nullable;

@Name("Last Registered Recipe")
@Description("Gets the recipe that was most recently registered through the Register Recipe Section.")
@Examples({
	"register a new shapeless recipe with the key \"my_recipe\":",
		"\tset ingredients to diamond, emerald and iron ingot",
		"\tset recipe result to diamond sword",
	"set {_last} to last registered recipe"
})
@Since("INSERT VERSION")
public class ExprLastRecipe extends SimpleExpression<Recipe> {

	static {
		Skript.registerExpression(ExprLastRecipe.class, Recipe.class, ExpressionType.SIMPLE, "[the] last (created|registered) recipe");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return true;
	}

	@Override
	protected Recipe @Nullable [] get(Event event) {
		return new Recipe[]{SecRegisterRecipe.lastRegistered};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<Recipe> getReturnType() {
		return Recipe.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the last registered recipe";
	}

}
