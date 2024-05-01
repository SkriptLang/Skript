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
import ch.njol.skript.bukkitutil.ServerUtils;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.ServerTickManager;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Server Tick State")
@Description("Represents the ticking state of the server, for example, if the server is frozen, or running normally.")
@Examples({"if server's tick state is currently frozen:", "if server tick state is normal:"})
@Since("INSERT VERSION")
@RequiredPlugins("Minecraft 1.20.4+")
public class CondServerTickState extends Condition {
	private ServerState state;

	static {
		if (Skript.methodExists(Bukkit.class, "getServerTickManager"))
			Skript.registerCondition(CondServerTickState.class,
				"[the] server['s] tick[ing] state is [currently] (:frozen|:stepping|:sprinting|:normal)",
				"[the] server['s] tick[ing] state (is[n't| not]) [currently] (:frozen|:stepping|:sprinting|:normal)");
	}

	public enum ServerState {
		FROZEN, STEPPING, SPRINTING, NORMAL
	}


	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (parseResult.hasTag("stepping")) {
			state = ServerState.STEPPING;
		} else if (parseResult.hasTag("sprinting")) {
			state = ServerState.SPRINTING;
		} else if (parseResult.hasTag("frozen")) {
			state = ServerState.FROZEN;
		} else if (parseResult.hasTag("normal")) {
			state = ServerState.NORMAL;
		}

		return true;
	}

	@Override
	public boolean check(Event event) {
		ServerTickManager serverTickManager = ServerUtils.getServerTickManager();
		switch (state) {
			case FROZEN:
				return serverTickManager.isFrozen() != isNegated();
			case STEPPING:
				return serverTickManager.isStepping() != isNegated();
			case SPRINTING:
				return serverTickManager.isSprinting() != isNegated();
			case NORMAL:
				return serverTickManager.isRunningNormally() != isNegated();
		}
		return isNegated();
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the server's tick state is " + state;
	}
}

