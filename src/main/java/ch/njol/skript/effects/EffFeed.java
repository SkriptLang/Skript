package ch.njol.skript.effects;

import ch.njol.skript.Skript;
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

@Name("Feed")
@Description("Feeds the specified players.")
@Examples({"feed all players", "feed the player by 5 beefs"})
@Since("2.2-dev34")
public class EffFeed extends Effect {

	static {
		Skript.registerEffect(EffFeed.class, "feed [the] %players% [by %-number% [beef[s]]]");
	}

	private Expression<Player> players;
	private @Nullable Expression<Number> beefs;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern,
						Kleenean isDelayed, ParseResult parseResult) {
		players = (Expression<Player>) expressions[0];
		beefs = (Expression<Number>) expressions[1];
		return true;
	}

	@Override
	protected void execute(Event event) {
		int level = 20;

		if (beefs != null) {
			Number levelNum = beefs.getSingle(event);
			if (levelNum == null)
				return;

			level = levelNum.intValue();
		}

		for (Player player : players.getArray(event)) {
			player.setFoodLevel(player.getFoodLevel() + level);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "feed " + players.toString(event, debug) +
			(beefs != null ? " by " + beefs.toString(event, debug) : "");
	}

}
