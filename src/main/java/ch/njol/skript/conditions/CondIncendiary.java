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

import org.bukkit.entity.Entity;
import org.bukkit.entity.Explosive;
import org.bukkit.event.Event;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.util.Kleenean;

@Name("Is Incendiary")
@Description("Checks if an entity will create fire when it explodes. This condition is also usable in an explosion prime event.")
@Examples({"on explosion prime:", 
			"\tif the explosion is fiery:",
			"\t\tbroadcast \"A fiery explosive has been ignited!\""})
@Since("2.5")
public class CondIncendiary extends Condition {

	static {
		Skript.registerCondition(CondIncendiary.class,
				"%entities% ((is|are) incendiary|cause[s] a[n] (incendiary|fiery) explosion)",
				"%entities% ((is not|are not|isn't|aren't) incendiary|(does not|do not|doesn't|don't) cause[s] a[n] (incendiary|fiery) explosion)",
				"the [event(-| )]explosion (is|1¦(is not|isn't)) (incendiary|fiery)"
		);
	}

	@SuppressWarnings("null")
	private Expression<Entity> entities;

	private boolean isEvent;

	@Override
	@SuppressWarnings({"unchecked", "null"})
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		isEvent = matchedPattern == 2;
		if (isEvent && !getParser().isCurrentEvent(ExplosionPrimeEvent.class)) {
			Skript.error("Checking if 'the explosion' is fiery is only possible in an explosion prime event", ErrorQuality.SEMANTIC_ERROR);
			return false;
		}
		if (!isEvent)
			entities = (Expression<Entity>) exprs[0];
		setNegated(matchedPattern == 1 || parseResult.mark == 1);
		return true;
	}

	@Override
	public boolean check(Event e) {
		if (isEvent)
			return ((ExplosionPrimeEvent) e).getFire() ^ isNegated();
		return entities.check(e, entity -> entity instanceof Explosive && ((Explosive) entity).isIncendiary(), isNegated());
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		if (isEvent)
			return "the event-explosion " + (isNegated() == false ? "is" : "is not") + " incendiary";
		if (entities.isSingle())
			return entities.toString(e, debug) + (isNegated() == false ? " is" : " is not") + " incendiary";
		return entities.toString(e, debug) + (isNegated() == false ? " are" : " are not") + " incendiary";
	}

}
