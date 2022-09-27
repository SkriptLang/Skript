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
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.doc.Events;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.BrewingStandFuelEvent;
import org.eclipse.jdt.annotation.Nullable;

@Name("Fuel Will Consume")
@Description("Gets whether the brewing stand's fuel will be reduced / consumed or not.")
@Examples({
	"on fuel brewing:",
	"\tif fuel will be consumed:",
	"\t\tset consume fuel to false"
})
@Events("fuel brewing")
@Since("INSERT VERSION")
public class CondWillConsume extends Condition {

	static {
		Skript.registerCondition(CondWillConsume.class, "[the] fuel (:will|will not|won't) [be] consume[d]");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if(getParser().isCurrentEvent(BrewingStandFuelEvent.class)) {
			Skript.error("You can't use the 'fuel will consume' condition outside of a fuel brewing event.");
			return false;
		}
		setNegated(!parseResult.hasTag("will"));
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (!(event instanceof BrewingStandFuelEvent))
			return false;
		return ((BrewingStandFuelEvent) event).isConsuming() ^ isNegated();
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the fuel " + (isNegated() ? "will" : "will not") + " consume";
	}
}
