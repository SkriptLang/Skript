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

public class EffStepServer extends Effect {

	static {
		Skript.registerEffect(EffStepServer.class,
			"make [the] server step for %timespan%",
			"make [the] server stop stepping");
	}

	private boolean step;
	private Expression<Timespan> timespan;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		step = matchedPattern == 0;
		if (step) {
			timespan = (Expression<Timespan>) exprs[0];
		}
		return true;
	}

	@Override
	protected void execute(Event event) {
		if (step) {
			long stepTicks = timespan != null ? timespan.getSingle(event).getTicks_i() : 1;
			Bukkit.getServer().getServerTickManager().stepGameIfFrozen((int) stepTicks);
		} else {
			Bukkit.getServer().getServerTickManager().stopStepping();
		}
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return step ? "step server" : "stop stepping server";
	}
}
