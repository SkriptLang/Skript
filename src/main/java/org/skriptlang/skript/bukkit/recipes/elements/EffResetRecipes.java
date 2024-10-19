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
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Reset Recipes")
@Description("Resets the recipes of the server; not including Minecraft defaults and datapacks.")
@Examples("reset server recipes")
@Since("INSERT VERSION")
public class EffResetRecipes extends Effect {

	static {
		Skript.registerEffect(EffResetRecipes.class,
			"reset [the] server['s] recipes");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return true;
	}

	@Override
	protected void execute(Event event) {
		Bukkit.resetRecipes();
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "reset server recipes";
	}

}
