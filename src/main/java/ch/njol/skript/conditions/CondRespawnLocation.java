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
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.eclipse.jdt.annotation.Nullable;

@Name("Is Bed/Anchor Spawn")
@Description("Checks what the respawn location of a player in the respawn event is.")
@Examples({
	"on respawn:",
	"\tthe respawn location is a bed",
	"\tbroadcast \"%player% is respawning in their bed! So cozy!\""
})
@RequiredPlugins("Minecraft 1.16+")
@Since("INSERT VERSION")
@Events("respawn")
public class CondRespawnLocation extends Condition {

	static {
		if (Skript.classExists("org.bukkit.block.data.type.RespawnAnchor"))
			Skript.registerCondition(CondRespawnLocation.class, "[the] respawn location (was|is)[1:(n'| no)t] [a] (:bed|respawn anchor)");
	}

	private boolean bedSpawn;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(PlayerRespawnEvent.class)) {
			Skript.error("The 'respawn location' condition may only be used in a respawn event");
			return false;
		}
		setNegated(parseResult.mark == 1);
		bedSpawn = parseResult.hasTag("bed");
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (event instanceof PlayerRespawnEvent) {
			PlayerRespawnEvent respawnEvent = (PlayerRespawnEvent) event;
			return (bedSpawn ? respawnEvent.isBedSpawn() : respawnEvent.isAnchorSpawn()) != isNegated();
		}
		return false;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the respawn location " + (isNegated() ? "isn't" : "is") + " a " + (bedSpawn ? "bed spawn" : "respawn anchor");
	}

}
