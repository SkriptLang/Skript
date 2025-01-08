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

@Name("Toggle Flight")
@Description("Toggle the <a href='expressions.html#ExprFlightMode'>flight mode</a> of a player.")
@Examples("allow flight to event-player")
@Since("2.3")
public class EffToggleFlight extends Effect {

	static {
		Skript.registerEffect(EffToggleFlight.class,
			"(allow|enable) (fly|flight) (for|to) %players%",
			"(disallow|disable) (fly|flight) (for|to) %players%");
	}

	private Expression<Player> players;
	private boolean allow;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		players = (Expression<Player>) exprs[0];
		allow = matchedPattern == 0;
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (Player player : players.getArray(event))
			player.setAllowFlight(allow);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "allow flight to " + players.toString(event, debug);
	}

}
