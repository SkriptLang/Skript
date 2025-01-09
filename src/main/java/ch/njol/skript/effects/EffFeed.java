package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.log.runtime.SyntaxRuntimeErrorProducer;

@Name("Feed")
@Description("Feeds the specified players.")
@Examples({"feed all players", "feed the player by 5 beefs"})
@Since("2.2-dev34")
public class EffFeed extends Effect implements SyntaxRuntimeErrorProducer {

    static {
        Skript.registerEffect(EffFeed.class, "feed [the] %players% [by %-number% [beef[s]]]");
    }

	private Node node;
    private Expression<Player> players;
    private @Nullable Expression<Number> beefs;

    @Override
	@SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		node = getParser().getNode();
        players = (Expression<Player>) exprs[0];
        beefs = (Expression<Number>) exprs[1];
        return true;
    }

    @Override
    protected void execute(Event event) {
        int level = 20;
        if (beefs != null) {
            Number beefs = this.beefs.getSingle(event);
            if (beefs == null) {
				error("The provided amount was not set.", this.beefs.toString());
				return;
			}
            level = beefs.intValue();
        }

        for (Player player : players.getArray(event)) {
            player.setFoodLevel(player.getFoodLevel() + level);
        }
    }

	@Override
	public Node getNode() {
		return node;
	}

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "feed " + players.toString(event, debug) + (beefs != null ? " by " + beefs.toString(event, debug) : "");
    }

}
