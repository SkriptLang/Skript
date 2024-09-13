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
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.jetbrains.annotations.Nullable;

@Name("Egg Will Hatch")
@Description("Whether the egg will hatch in a Player Egg Throw event.")
@Examples({
	"on player egg throw:",
	"\tif an entity won't hatch:",
	"\t\tsend \"Better luck next time!\" to the player"
})
@Events("Egg Throw")
@Since("2.7")
public class CondWillHatch extends Condition {

	static {
		Skript.registerCondition(CondWillHatch.class,
				"[the] egg (:will|will not|won't) hatch"
		);
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(PlayerEggThrowEvent.class)) {
			Skript.error("You can't use the 'egg will hatch' condition outside of a Player Egg Throw event.");
			return false;
		}
		setNegated(!parseResult.hasTag("will"));
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (!(event instanceof PlayerEggThrowEvent))
			return false;
		return ((PlayerEggThrowEvent) event).isHatching() ^ isNegated();
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the egg " + (isNegated() ? "will" : "will not") + " hatch";
	}

}
