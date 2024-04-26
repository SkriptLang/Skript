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

import ch.njol.skript.Skript;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.ServerTickManager;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Sprint Server")
@Description({
	"Requests the server to sprint for a certain amount of time, or stops the server from sprinting.",
	"Sprinting is where the server increases the tick rate depending on the time you input, and resets it to what it was after its done sprinting."
})
@Examples({"request server to sprint for 10 seconds", "make server stop sprinting"})
@Since("INSERT VERSION")
@RequiredPlugins("Minecraft 1.20.4+")
public class EffSprintServer extends Effect {

	private static final ServerTickManager SERVER_TICK_MANAGER;

	static {
		ServerTickManager STM_VALUE = null;
		if (Skript.methodExists(Server.class, "getServerTickManager")) {
			STM_VALUE = Bukkit.getServerTickManager();
			Skript.registerEffect(EffSprintServer.class,
				"request [for [the]] server [to] sprint for %timespan%",
				"make [the] server stop sprinting");
		}
		SERVER_TICK_MANAGER = STM_VALUE;
	}

	private boolean sprint;
	private Expression<Timespan> timespan;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		sprint = matchedPattern == 0;
		if (sprint)
			timespan = (Expression<Timespan>) exprs[0];
		return true;
	}

	@Override
	protected void execute(Event event) {
		if (sprint) {
			Timespan timespanInstance = timespan.getSingle(event);
			long sprintTicks = timespanInstance != null ? timespanInstance.getTicks() : 1;
			SERVER_TICK_MANAGER.requestGameToSprint((int) sprintTicks);
		} else {
			SERVER_TICK_MANAGER.stopSprinting();
		}
	}


	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return sprint ? "request to sprint server for" + timespan.toString(event, debug) : "stop sprinting server";
	}
}
