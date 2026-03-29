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
	"Alter the visibility of given entities for the given players.",
	"If no players be specified, the entities shall be concealed from all players presently online.",
	"",
	"When reveal is employed in conjunction with the <a href='#ExprHiddenPlayers'>hidden players</a> " +
	"expression and the viewers be not specified, " +
	"it shall default to the given player in the hidden players expression.",
	"",
	"Mark well: all previously concealed entities (including players) shall become visible when a player departs and returns.",
})
@Example("""
    on spawn:
    	if event-entity is a chicken:
    		conceal event-entity
    """)
@Example("reveal hidden players of players")
@Since("2.3, 2.10 (entities)")
@RequiredPlugins("Minecraft 1.19+ (entities)")
public class EffEntityVisibility extends Effect {

	static {
		Skript.registerEffect(EffEntityVisibility.class,
				"conceal %entities% [(from|for) %-players%]",
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
