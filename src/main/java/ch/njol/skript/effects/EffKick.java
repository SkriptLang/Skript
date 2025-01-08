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
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.jetbrains.annotations.Nullable;

@Name("Kick")
@Description("Kicks a player from the server.")
@Examples({
	"on place of TNT, lava, or obsidian:",
		"\tkick the player due to \"You may not place %block%!\"",
		"\tcancel the event"
})
@Since("1.0")
public class EffKick extends Effect {

	static {
		Skript.registerEffect(EffKick.class, "kick %players% [(by reason of|because [of]|on account of|due to) %-string%]");
	}
	
	private Expression<Player> players;
	private @Nullable Expression<String> reason;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		players = (Expression<Player>) exprs[0];
		reason = (Expression<String>) exprs[1];
		return true;
	}

	@Override
	protected void execute(Event event) {
		String reason = this.reason != null ? this.reason.getSingle(event) : "";
		if (reason == null)
			return;
		for (Player player : players.getArray(event)) {
			if (event instanceof PlayerLoginEvent playerLoginEvent && player.equals(playerLoginEvent.getPlayer()) && !Delay.isDelayed(event)) {
				playerLoginEvent.disallow(Result.KICK_OTHER, reason);
			} else if (event instanceof PlayerKickEvent playerKickEvent && player.equals(playerKickEvent.getPlayer()) && !Delay.isDelayed(event)) {
				playerKickEvent.setLeaveMessage(reason);
			} else {
				player.kickPlayer(reason);
			}
		}
	}
	
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "kick " + players.toString(event, debug) + (reason != null ? " on account of " + reason.toString(event, debug) : "");
	}
	
}
