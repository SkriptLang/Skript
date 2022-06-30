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

package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.eclipse.jdt.annotation.Nullable;


public class CondRespawnLocation extends Condition {

	static {
		Skript.registerCondition(CondRespawnLocation.class, "[the] respawn location (was|is)(0¦|1¦n('|o)t) [a[n]] (:bed|[respawn] anchor) [spawn]");
	}

	private boolean bedSpawn;

	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final SkriptParser.ParseResult parseResult) {
		if (!getParser().isCurrentEvent(PlayerRespawnEvent.class)) {
			Skript.error("The condition 'respawn location' may only be used in the respawn event", ErrorQuality.SEMANTIC_ERROR);
			return false;
		}
		setNegated(parseResult.mark == 1);
		bedSpawn = parseResult.hasTag("bed");
		return true;
	}

	@Override
	public boolean check(final Event e) {
		final PlayerRespawnEvent event = (PlayerRespawnEvent) e;
		if(bedSpawn) return event.isBedSpawn() != isNegated();
		else return event.isAnchorSpawn() != isNegated();
	}

	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		final PlayerRespawnEvent event = (PlayerRespawnEvent) e;
		return "the respawn location" + (isNegated() ? " isn't" : " is") + " a" + (event.isAnchorSpawn() ? " respawn anchor" : " bed");
	}

}
