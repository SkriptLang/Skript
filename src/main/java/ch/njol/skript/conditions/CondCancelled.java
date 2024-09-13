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
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Event Cancelled")
@Description("Checks whether or not the event is cancelled.")
@Examples({"on click:",
		"\tif event is cancelled:",
		"\t\tbroadcast \"no clicks allowed!\""
})
@Since("2.2-dev36")
public class CondCancelled extends Condition {

	static {
		Skript.registerCondition(CondCancelled.class,
				"[the] event is cancel[l]ed",
				"[the] event (is not|isn't) cancel[l]ed"
		);
	}
	
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setNegated(matchedPattern == 1);
		return true;
	}

	@Override
	public boolean check(Event e) {
		return (e instanceof Cancellable && ((Cancellable) e).isCancelled()) ^ isNegated();
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return isNegated() ? "event is not cancelled" : "event is cancelled";
	}

}
