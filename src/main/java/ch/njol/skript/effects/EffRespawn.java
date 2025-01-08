package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

@Name("Force Respawn")
@Description({
	"Forces player(s) to respawn if they are dead.",
	"If this is called without delay from death event, one tick is waited before respawn attempt."
})
@Examples({
	"on death of player:",
		"\tforce event-player to respawn"
})
@Since("2.2-dev21")
public class EffRespawn extends Effect {

	static {
		Skript.registerEffect(EffRespawn.class, "force %players% to respawn");
	}

	private Expression<Player> players;
	private boolean forceDelay;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (getParser().isCurrentEvent(PlayerRespawnEvent.class)) { // Just in case someone tries to do this
			Skript.error("Respawning the player in a respawn event is not possible", ErrorQuality.SEMANTIC_ERROR);
			return false;
		}
		players = (Expression<Player>) exprs[0];
		// Force a delay before respawning the player if we're in the death event and there isn't already a delay
		// Unexpected behavior may occur if we don't do this
		forceDelay = getParser().isCurrentEvent(EntityDeathEvent.class) && isDelayed.isFalse();
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (Player player : players.getArray(event)) {
			if (forceDelay) { // Use Bukkit runnable
				new BukkitRunnable() {

					@Override
					public void run() {
						player.spigot().respawn();
					}

				}.runTaskLater(Skript.getInstance(), 1);
			} else { // Just respawn
				player.spigot().respawn();
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "force " + players.toString(event, debug) + " to respawn";
	}

}
