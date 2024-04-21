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
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

public class CondServerState extends Condition {

	static {
		if (Skript.methodExists(Server.class, "getServerTickManager")) {
			Skript.registerCondition(CondServerState.class,
				"server state is [currently] (:frozen|:stepping|:sprinting|:normal)",
				"server state (is[n't| not]) [currently] (:frozen|:stepping|:sprinting|:normal)");
		}
	}

	private String state;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (parseResult.hasTag("stepping")) {
			state = "stepping";
		} else if (parseResult.hasTag("sprinting")) {
			state = "sprinting";
		} else if (parseResult.hasTag("frozen")) {
			state = "frozen";
		} else if (parseResult.hasTag("normal")) {
			state = "normal";
		}

		return true;
	}

	@Override
	public boolean check(Event e) {
		switch (state) {
			case "frozen":
				return Bukkit.getServer().getServerTickManager().isFrozen();
			case "stepping":
				return Bukkit.getServer().getServerTickManager().isStepping();
			case "sprinting":
				return Bukkit.getServer().getServerTickManager().isSprinting();
			case "normal":
				return Bukkit.getServer().getServerTickManager().isRunningNormally();
		}
		return false;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "server state is " + state;
	}

}
