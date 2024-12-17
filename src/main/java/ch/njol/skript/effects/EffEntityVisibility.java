package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.ExprHiddenPlayers;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

@Name("Entity Visibility")
@Description({
	"Change visibility of the given entities for the given players.",
	"If no players are given, will hide the entities from all online players.",
	"",
	"When reveal is used in combination of the <a href='expressions.html#ExprHiddenPlayers'>hidden players</a> " +
	"expression and the viewers are not specified, " +
	"this will default it to the given player in the hidden players expression.",
	"",
	"Note: all previously hidden entities (including players) will be visible when a player leaves and rejoins.",
})
@Examples({
	"on spawn:",
		"\tif event-entity is a chicken:",
			"\t\thide event-entity",
	"",
	"reveal hidden players of players"
})
@Since("2.3, INSERT VERSION (entities)")
@RequiredPlugins("Minecraft 1.19+ (entities)")
public class EffEntityVisibility extends Effect {

	static {
		Skript.registerEffect(EffEntityVisibility.class,
				"hide %entities% [(from|for) %-players%]",
				"reveal %entities% [(to|for|from) %-players%]");
	}

	private boolean reveal;

	@UnknownNullability
	private Expression<Entity> hidden;
	@UnknownNullability
	private Expression<Player> viewers;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult result) {
		reveal = matchedPattern == 1;
		hidden = (Expression<Entity>) exprs[0];

		if (reveal && exprs[0] instanceof ExprHiddenPlayers && exprs.length == 1) {
			viewers = ((ExprHiddenPlayers) exprs[0]).getViewers();
		} else {
			viewers = (Expression<Player>) exprs[1];
		}

		return true;
	}

    @Override
    protected void execute(Event event) {
		Player[] updated = viewers != null ? viewers.getArray(event) : Bukkit.getOnlinePlayers().toArray(new Player[0]);

		Skript instance = Skript.getInstance();
		if (reveal) {
			for (Player player : updated) {
				for (Entity entity : hidden.getArray(event)) {
					player.showEntity(instance, entity);
				}
			}
		} else {
			for (Player player : updated) {
				for (Entity entity : hidden.getArray(event)) {
					player.hideEntity(instance, entity);
				}
			}
		}
    }

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (reveal ? "reveal " : "hide ") + "entities " +
				hidden.toString(event, debug) +
				(reveal ? " to " : " from ") +
				viewers.toString(event, debug);
	}

}
