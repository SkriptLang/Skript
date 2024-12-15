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
