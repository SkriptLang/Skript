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
import ch.njol.skript.command.EffectCommandEvent;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Effect Command Is Valid")
@Description("Checks whether the parsed effect command in an 'effect command' event is valid.")
@Examples({
	"if the effect command is valid:",
		"\tsend \"%sender% has executed an invalid effect command!\" to console"
})
@Since("INSERT VERSION")
public class CondEffectCommandIsValid extends Condition {

	static {
		Skript.registerCondition(CondEffectCommandIsValid.class, "[the] effect command is [not:(not |in)]valid");
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		if (!getParser().isCurrentEvent(EffectCommandEvent.class)) {
			Skript.error("The 'effect command is valid' condition can only be used in an 'effect command' event");
			return false;
		}
		setNegated(parseResult.hasTag("not"));
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (!(event instanceof EffectCommandEvent))
			return false;
		return ((EffectCommandEvent) event).getEffect() != null ^ isNegated();
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "effect command is " + (isNegated() ? "in" : "") + "valid";
	}

}
