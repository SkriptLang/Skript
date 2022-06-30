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
import ch.njol.util.Kleenean;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

public class CondAnchorWorks extends Condition {

	static {
		if(Skript.isRunningMinecraft(1, 16)) {
			Skript.registerCondition(CondAnchorWorks.class, "[respawn] anchor[s] (0¦[do]|1¦do(n'| no)t) work in %worlds%");
		}
	}

	Expression<World> worlds;

	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final SkriptParser.ParseResult parseResult) {
		worlds = (Expression<World>) exprs[0];
		setNegated(parseResult.mark == 1);
		return true;
	}

	@Override
	public boolean check(final Event e) {
		for(World world : worlds.getArray(e)) {
			return world.isRespawnAnchorWorks() != isNegated();
		}
		return false;
	}

	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "respawn anchors " + (isNegated() ? " do" : " don't") + " work in " + worlds.toString(e, debug);
	}

}
