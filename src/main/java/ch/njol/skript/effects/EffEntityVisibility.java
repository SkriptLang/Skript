package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.ExprHiddenPlayers;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Entity Visibility")
@Description({
	"Change visibility of the given entities for the given players.",
	"If no players are given, will hide the entities from all online players.",
	"",
	"When reveal is used in combination of the <a href='expressions.html#ExprHiddenPlayers'>hidden players</a> " +
	"expression and the viewers are not specified, " +
	"this will default it to the given player in the hidden players expression.",
	"",
	"Note: if a player was hidden and relogs, this player will be visible again."
})
@Examples({
	"on spawn:",
		"\tif event-entity is a chicken:",
			"\t\thide event-entity",
	"",
	"reveal hidden players of players"
})
@Since("INSERT VERSION")
@RequiredPlugins("Minecraft 1.19+")
public class EffEntityVisibility extends Effect {

	static {
		Skript.registerEffect(EffEntityVisibility.class,
				"hide %entities% [(from|for) %-players%]",
				"reveal %entities% [(to|for|from) %-players%]");
	}

	private boolean reveal;

	@Nullable
	private Expression<Entity> hidden;
	@Nullable
	private Expression<Player> viewers;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern,
						Kleenean isDelayed, SkriptParser.ParseResult result) {
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
    protected void execute(Event e) {
		Player[] updatedViewers;
		if (viewers != null) {
			updatedViewers = viewers.getArray(e);
		} else {
			updatedViewers = Bukkit.getOnlinePlayers().toArray(new Player[0]);
		}

		for (Player player : updatedViewers) {
            for (Entity entity : hidden.getArray(e)) {
                if (reveal) {
					player.showEntity(Skript.getInstance(), entity);
                } else {
					player.hideEntity(Skript.getInstance(), entity);
                }
            }
        }
    }

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (reveal ? "reveal " : "hide ") + "entities " +
				hidden.toString(event, debug) +
				(reveal ? " to " : " from ") +
				(viewers != null ? viewers.toString(event, debug) : "");
	}
}
