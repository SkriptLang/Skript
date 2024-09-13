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
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.jetbrains.annotations.Nullable;

@Name("Make Egg Hatch")
@Description("Makes the egg hatch in a Player Egg Throw event.")
@Examples({
	"on player egg throw:",
		"\t# EGGS FOR DAYZ!",
		"\tmake the egg hatch"
})
@Events("Egg Throw")
@Since("2.7")
public class EffMakeEggHatch extends Effect {

	static {
		Skript.registerEffect(EffMakeEggHatch.class,
				"make [the] egg [:not] hatch"
		);
	}

	private boolean not;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(PlayerEggThrowEvent.class)) {
			Skript.error("You can't use the 'make the egg hatch' effect outside of a Player Egg Throw event.");
			return false;
		}
		not = parseResult.hasTag("not");
		return true;
	}

	@Override
	protected void execute(Event e) {
		if (e instanceof PlayerEggThrowEvent) {
			PlayerEggThrowEvent event = (PlayerEggThrowEvent) e;
			event.setHatching(!not);
			if (!not && event.getNumHatches() == 0) // Make it hatch something!
				event.setNumHatches((byte) 1);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "make the egg " + (not ? "not " : "") + "hatch";
	}

}
