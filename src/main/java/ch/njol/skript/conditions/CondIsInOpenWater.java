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
import org.bukkit.event.player.PlayerFishEvent;
import org.eclipse.jdt.annotation.Nullable;

@Name("Is in Open Water")
@Description("Check whether or not the fish hook is in open water.")
@Examples({"on fish:",
		"\tif hook is in open water:",
		"\t\tsend \"You will catch a shark soon!\""})
@Events("fishing")
@Since("INSERT VERSION")
public class CondIsInOpenWater extends Condition {
	
	static {
		Skript.registerCondition(CondIsInOpenWater.class,
				"[fish[ing]] hook (is|are) in open water",
				"[fish[ing]] hook (isn't|is not|aren't|are not) in open water");
	}
	
	@Override
	@SuppressWarnings("null")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(PlayerFishEvent.class)) {
			Skript.error("The 'hook is in open water' condition can only be used in fish event.");
			return false;
		}
		setNegated(matchedPattern == 1);
		return true;
	}
	
	@Override
	public boolean check(Event e) {
		return isNegated() ^ ((PlayerFishEvent) e).getHook().isInOpenWater();
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "hook is" + (isNegated() ? "n't" : "") + " in open water";
	}
	
}
