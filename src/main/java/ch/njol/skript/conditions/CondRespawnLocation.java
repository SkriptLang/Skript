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
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.eclipse.jdt.annotation.Nullable;

@Name("Is Bed/Anchor Spawn")
@Description("Checks what the respawn location of a player in the respawn event is.")
@Examples({"respawn location is a bed"})
@RequiredPlugins("Minecraft 1.16+")
@Since("INSERT VERSION")
public class CondRespawnLocation extends Condition {

	static {
		if (Skript.classExists("org.bukkit.block.data.type.RespawnAnchor"))
			Skript.registerCondition(CondRespawnLocation.class, "[the] respawn location (was|is)[1:(n'| no)t] [a] (:bed|respawn anchor)");
	}

	private boolean bedSpawn;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(PlayerRespawnEvent.class)) {
			Skript.error("The condition 'respawn location' may only be used in the respawn event");
			return false;
		}
		setNegated(parseResult.mark == 1);
		bedSpawn = parseResult.hasTag("bed");
		return true;
	}

	@Override
	public boolean check(Event e) {
		PlayerRespawnEvent event = (PlayerRespawnEvent) e;
		if (bedSpawn)
			return event.isBedSpawn() != isNegated();
		else
			return event.isAnchorSpawn() != isNegated();
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "the respawn location " + (isNegated() ? "isn't" : "is") + " a " + (bedSpawn ? "bed spawn" : "respawn anchor");
	}

}
