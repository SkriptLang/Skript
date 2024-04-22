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
import ch.njol.skript.lang.SkriptParser;
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
@Name("Step Server")
@Description("Makes the server step for a certain amount of time if the server state is frozen, or stops the server from stepping.")
@Examples({"make server step for 5 seconds if server is frozen", "make server stop stepping"})
@Since("INSERT VERSION")
@RequiredPlugins("Minecraft 1.20.4+")
public class EffStepServer extends Effect {

	private static final ServerTickManager SERVER_TICK_MANAGER;

	static {
		if (Skript.methodExists(Server.class, "getServerTickManager")) {
			SERVER_TICK_MANAGER = Bukkit.getServerTickManager();
			Skript.registerEffect(EffStepServer.class,
				"make [the] server step for %timespan%",
				"make [the] server stop stepping");
		} else {
			SERVER_TICK_MANAGER = null;
		}
	}

	private Expression<Timespan> timespan;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		if (matchedPattern == 0)
			timespan = (Expression<Timespan>) exprs[0];
		return true;
	}

	@Override
	protected void execute(Event event) {
		Timespan timespanInstance = timespan.getSingle(event);
		if (timespanInstance != null) {
			long stepTicks = timespanInstance.getTicks();
			SERVER_TICK_MANAGER.stepGameIfFrozen((int) stepTicks);
		} else {
			SERVER_TICK_MANAGER.stopStepping();
		}
	}


	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return timespan == null ? "make the server stop stepping" : "make the step server for " + timespan.toString(e, debug);
	}
}
