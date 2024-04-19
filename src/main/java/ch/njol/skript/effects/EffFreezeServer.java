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
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public class EffFreezeServer extends Effect {

	static {
		Skript.registerEffect(EffFreezeServer.class,
			"freeze [the] server",
			"unfreeze [the] server");
	}

	private boolean freeze;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		freeze = matchedPattern == 0;
		return true;
	}

	@Override
	protected void execute(Event event) {
		Bukkit.getServer().getServerTickManager().setFrozen(freeze);
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return freeze ? "freeze server effect" : "unfreeze server effect";
	}
}
