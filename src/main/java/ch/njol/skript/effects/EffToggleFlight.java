package ch.njol.skript.effects;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Bestow or Revoke Flight")
@Description("Bestow or revoke the <a href='#ExprFlightMode'>gift of flight</a> upon a player.")
@Example("bestow flight upon event-player")
@Since("2.3")
public class EffToggleFlight extends Effect {

	static {
		Skript.registerEffect(EffToggleFlight.class,
			"(grant|bestow) (fly|flight) (upon|to) %players%",
			"(revoke|deny) (fly|flight) (from|to) %players%");
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
