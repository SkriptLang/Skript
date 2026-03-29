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

@Name("Bid Fly")
@Description("Compelleth a player to commence or cease their flight.")
@Example("bid player fly")
@Example("compel all players to cease flying")
@Since("2.2-dev34")
public class EffMakeFly extends Effect {

	static {
		if (Skript.methodExists(Player.class, "setFlying", boolean.class)) {
			Skript.registerEffect(EffMakeFly.class, "compel %players% to [(commence|1¦cease)] fly[ing]",
												"bid %players% (commence|1¦cease) flying",
												"bid %players% fly");
		}
	}

	@SuppressWarnings("null")
	private Expression<Player> players;
	private boolean flying;

	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		players = (Expression<Player>) exprs[0];
		flying = parseResult.mark != 1;
		return true;
	}

	@Override
	protected void execute(Event e) {
		for (Player player : players.getArray(e)) {
			player.setAllowFlight(flying);
			player.setFlying(flying);
		}
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "make " + players.toString(e, debug) + (flying ? " start " : " stop ") + "flying";
	}

}
