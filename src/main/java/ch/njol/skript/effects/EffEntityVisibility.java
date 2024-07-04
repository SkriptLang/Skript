package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

@Name("Entity Visibility")
@Description({
	"Change visibility of an entity for the given players.",
	"If no players are given, will hide the entity from all online players."
})
@Examples({
	"on spawn:",
		"\tif event-entity is a chicken:" +
			"\t\thide event-entity"
})
@Since("INSERT VERSION")
@RequiredPlugins("Minecraft 1.19+")
public class EffEntityVisibility extends Effect {

	public static final boolean SUPPORTS_ENTITY_VISIBILITY =
		Skript.methodExists(Player.class, "hideEntity", Plugin.class, Entity.class)
		&& Skript.methodExists(Player.class, "showEntity", Plugin.class, Entity.class);

	static {
		if (SUPPORTS_ENTITY_VISIBILITY)
			Skript.registerEffect(EffEntityVisibility.class,
					"hide %entities% [(from|for) %-players%]",
					"reveal %entities% [(to|for|from) %-players%]");
	}

	private boolean reveal;

	@SuppressWarnings("null")
	private Expression<Entity> entities;

	@Nullable
	private Expression<Player> players;

	@Override
	@SuppressWarnings({"unchecked", "null"})
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		reveal = matchedPattern == 1;
		entities = (Expression<Entity>) exprs[0];
		players = exprs.length > 1 ? (Expression<Player>) exprs[1] : null;
		return true;
	}

    @Override
    @SuppressWarnings("null")
    protected void execute(Event e) {
		Player[] pls;
        if (players != null) {
			pls = players.getArray(e);
		} else {
			pls = Bukkit.getOnlinePlayers().toArray(new Player[0]);
		}

		for (Player player : pls) {
            for (Entity entity : entities.getArray(e)) {
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
		return (reveal ? "reveal " : "hide ")
			+ entities.toString(event, debug)
			+ (reveal ? " to " : " from ")
			+ (players != null ? players.toString(event, debug) : "");
	}
}