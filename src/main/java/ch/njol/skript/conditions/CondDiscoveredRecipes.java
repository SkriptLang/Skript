package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Has Discovered Recipe")
@Description("Checks whether a player or players have discovered a recipe.")
@Examples({
	"if player has discovered recipe \"custom_recipe\":",
		"\tgive player 1 diamond",
	"",
	"if all players have not found recipe \"custom_recipe\":",
		"\tkill all players",
})
@Since("INSERT VERSION")
public class CondDiscoveredRecipes extends Condition {

	static {
		Skript.registerCondition(CondDiscoveredRecipes.class,
			"%players% (has|have) (discovered|unlocked) recipe[s] %strings%",
			"%players% (hasn't|has not|haven't|have not) (discovered|unlocked) recipe[s] %strings%");
	}

	private Expression<Player> players;
	private Expression<String> recipes;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		players = (Expression<Player>) exprs[0];
		recipes = (Expression<String>) exprs[1];
		setNegated(matchedPattern == 1);
		return true;
	}

	@Override
	public boolean check(Event event) {
		return players.check(event,
			player -> recipes.check(event,
				recipe -> {
					NamespacedKey key = NamespacedKey.fromString(recipe, Skript.getInstance());
					if (Bukkit.getRecipe(key) != null)
						return player.hasDiscoveredRecipe(key);
					return false;
				}
			)
		);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return players.toString(event, debug) + (isNegated() ? "have not" : "have") + " found recipes " + recipes.toString(event, debug);
	}
}
