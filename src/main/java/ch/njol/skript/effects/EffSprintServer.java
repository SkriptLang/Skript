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
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public class EffSprintServer extends Effect {

	static {
		Skript.registerEffect(EffSprintServer.class,
			"make [the] server sprint for %timespan%",
			"make [the] server stop sprinting");
	}

	private boolean sprint;
	private Expression<Timespan> timespan;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		sprint = matchedPattern == 0;
		if (sprint) {
			timespan = (Expression<Timespan>) exprs[0];
		}
		return true;
	}

	@Override
	protected void execute(Event event) {
		if (sprint) {
			long sprintTicks = timespan != null ? timespan.getSingle(event).getTicks_i() : 1;
			Bukkit.getServer().getServerTickManager().requestGameToSprint((int) sprintTicks);
		} else {
			Bukkit.getServer().getServerTickManager().stopSprinting();
		}
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return sprint ? "sprint server effect" : "stop sprinting server effect";
	}
}
