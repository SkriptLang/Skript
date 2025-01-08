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
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("op/deop")
@Description("Grant/revoke a user operator state.")
@Examples({
	"op the player",
	"deop all players"
})
@Since("1.0")
public class EffOp extends Effect {
	
	static {
		Skript.registerEffect(EffOp.class, "[de[-]]op %offlineplayers%");
	}

	private Expression<OfflinePlayer> players;
	private boolean op;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		players = (Expression<OfflinePlayer>) exprs[0];
		op = !parseResult.expr.substring(0, 2).equalsIgnoreCase("de");
		return true;
	}
	
	@Override
	protected void execute(Event event) {
		for (OfflinePlayer player : players.getArray(event)) {
			player.setOp(op);
		}
	}
	
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (op ? "" : "de") + "op " + players.toString(event, debug);
	}
	
}
