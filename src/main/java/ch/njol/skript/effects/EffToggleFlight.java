package ch.njol.skript.effects;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

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

	@SuppressWarnings("null")
	private Expression<Player> players;

	private boolean allow;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		players = (Expression<Player>) exprs[0];
		allow = matchedPattern == 0;
		return true;
	}

	@Override
	protected void execute(final Event e) {
		for (Player player : players.getArray(e))
			player.setAllowFlight(allow);
	}

	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "allow flight to " + players.toString(e, debug);
	}
}
