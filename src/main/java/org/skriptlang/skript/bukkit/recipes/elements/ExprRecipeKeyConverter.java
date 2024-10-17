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
import ch.njol.skript.bukkitutil.NamespacedUtils;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

@Name("Recipe Key Converter")
@Description({
	"Converts a string to a valid key to be used with recipes.",
	"'using minecraft namespace' will convert the string to a minecraft namespaced key."
})
@Examples({
	"send the converted recipe key from \"my_recipe\"",
	"loop all custom recipes:",
		"\tif recipe key of loop-recipe = converted key from \"my_recipe\" using minecraft namespace:",
			"\t\tbroadcast loop-recipe",
	"",
	"register a new shaped recipe with the key (converted key from \"my_recipe\"):",
		"set the recipe ingredients to diamond, iron ingot and raw copper",
		"set the recipe result to diamond helmet"
})
@Since("INSERT VERSION")
public class ExprRecipeKeyConverter extends SimpleExpression<String> {

	static {
		Skript.registerExpression(ExprRecipeKeyConverter.class, String.class, ExpressionType.SIMPLE,
			"[the] converted [recipe] key[s] (from|of|for) %strings%",
			"[the] converted [recipe] key[s] (from|of|for) %strings% (using|with) (mc|minecraft) namespace");
	}

	private Expression<String> names;
	private boolean usingMinecraft;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		names = (Expression<String>) exprs[0];
		usingMinecraft = matchedPattern == 1;
		return true;
	}

	@Override
	protected String @Nullable [] get(Event event) {
		return Arrays.stream(names.getArray(event)).map(name -> NamespacedUtils.getNamespacedKey(name, usingMinecraft).toString()).toArray(String[]::new);
	}

	@Override
	public boolean isSingle() {
		return names.isSingle();
	}

	@Override
	public Class<String> getReturnType() {
		return String.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "recipe keys from " + names.toString(event, debug);
	}
}
