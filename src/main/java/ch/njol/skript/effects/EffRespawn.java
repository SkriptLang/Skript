/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.effects;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

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

@Name("Force Respawn")
@Description("Forces player(s) to respawn if they are dead. If this is called without delay from death event, one tick is waited before respawn attempt.")
@Examples({"on death of player:",
		"\tforce event-player to respawn",})
@Since("2.2-dev21")
public class EffRespawn extends Effect {

	static {
		Skript.registerEffect(EffRespawn.class, "force %players% to respawn");
	}

	@SuppressWarnings("null")
	private Expression<Player> players;

	private boolean forceDelay;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
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
	protected void execute(final Event e) {
		for (final Player p : players.getArray(e)) {
			if (forceDelay) { // Use Bukkit runnable
				new BukkitRunnable() {

					@Override
					public void run() {
						p.spigot().respawn();
					}

				}.runTaskLater(Skript.getInstance(), 1);
			} else { // Just respawn
				p.spigot().respawn();
			}
		}
	}

	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "force " + players.toString(e, debug) + " to respawn";
	}

}
