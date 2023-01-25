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
package ch.njol.skript.events;

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import com.destroystokyo.paper.event.player.PlayerStartSpectatingEntityEvent;
import com.destroystokyo.paper.event.player.PlayerStopSpectatingEntityEvent;

import ch.njol.skript.Skript;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.coll.CollectionUtils;

public class EvtSpectate extends SkriptEvent {

	static {
		if (Skript.classExists("com.destroystokyo.paper.event.player.PlayerStartSpectatingEntityEvent"))
			Skript.registerEvent("Spectate", EvtSpectate.class, CollectionUtils.array(PlayerStartSpectatingEntityEvent.class, PlayerStopSpectatingEntityEvent.class),
						"[player] stop spectating [(of|from) %-*entitydatas%]",
						"[player] (swap|switch) spectating [(of|from) %-*entitydatas%]",
						"[player] start spectating [of %-*entitydatas%]")
					.description("Called with a player starts, stops or swaps spectating an entity.")
					.examples("on player start spectating of a zombie:")
					.requiredPlugins("Paper")
					.since("INSERT VERSION");
	}

	private Literal<EntityData<?>> datas;

	private static final int STOP = -1, SWAP = 0, START = 1;

	/**
	 * 1 = swap. When the player did have a past spectating target.
	 * 0 = start. When the player starts spectating a new target.
	 * -1 = stop. When the player stops spectating a target.
	 */
	private int pattern;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		pattern = matchedPattern - 1;
		datas = (Literal<EntityData<?>>) args[0];
		return true;
	}

	@Override
	public boolean check(Event event) {
		boolean swap = false;
		Entity entity;
		// Start or swap event, and must be PlayerStartSpectatingEntityEvent.
		if (pattern != STOP && event instanceof PlayerStartSpectatingEntityEvent) {
			PlayerStartSpectatingEntityEvent spectating = (PlayerStartSpectatingEntityEvent) event;
			entity = spectating.getNewSpectatorTarget();

			// If it's a swap event, we're checking for past target on entity data and no null targets in the event.
			if (swap = pattern == SWAP && entity != null && spectating.getCurrentSpectatorTarget() != null)
				entity = spectating.getCurrentSpectatorTarget();
		} else if (event instanceof PlayerStopSpectatingEntityEvent) {
			entity = ((PlayerStopSpectatingEntityEvent) event).getSpectatorTarget();
		} else {
			// Swap event cannot be a stop spectating event.
			return false;
		}
		// Wasn't a swap event.
		if (pattern == SWAP && !swap)
			return false;
		if (datas == null)
			return true;
		for (EntityData<?> data : this.datas.getAll(event)) {
			if (data.isInstance(entity))
				return true;
		}
		return false;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (pattern == START ? "start" : pattern == SWAP ? "swap" : "stop") + " spectating" +
					(datas != null ? "of " + datas.toString(event, debug) : "");
	}

}
