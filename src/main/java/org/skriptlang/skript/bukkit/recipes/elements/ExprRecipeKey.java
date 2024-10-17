package org.skriptlang.skript.bukkit.recipes.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.Keyed;
import org.bukkit.event.Event;
import org.bukkit.inventory.Recipe;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Name("Recipe Name")
@Description("Get the namespaced key of a recipe.")
@Examples({
	"loop all recipes:",
		"\tbroadcast the recipe key of loop-recipe",
		"\tadd loop-recipe's id to {_list::*}"
})
@Since("INSERT VERSION")
public class ExprRecipeKey extends PropertyExpression<Recipe, String> {

	static {
		Skript.registerExpression(ExprRecipeKey.class, String.class, ExpressionType.PROPERTY,
			"[the] recipe (key|id)[s] of %recipes%",
			"[the] %recipes%'[s] (key|id)[s]");
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr((Expression<Recipe>) exprs[0]);
		return true;
	}

	@Override
	protected String @Nullable [] get(Event event, Recipe[] source) {
		List<String> names = new ArrayList<>();
		for (Recipe recipe : getExpr().getArray(event)) {
			if (recipe instanceof Keyed key)
				names.add(key.getKey().toString());
		}
		return names.toArray(new String[0]);
	}

	@Override
	public Class<String> getReturnType() {
		return String.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the recipe keys of " + getExpr().toString(event, debug);
	}
}
