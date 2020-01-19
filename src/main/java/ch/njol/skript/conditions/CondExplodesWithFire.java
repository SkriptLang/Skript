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
 *
 * Copyright 2011-2017 Peter Güttinger and contributors
 */
package ch.njol.skript.conditions;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Explosive;
import org.bukkit.event.Event;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.ScriptLoader;
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

@Name("Explodes With Fire")
@Description("Checks if an entity will create fire if it explodes.")
@Examples({"on explosion prime:", 
			"\tif the event-explosion is fiery:",
			"\t\tbroadcast \"A fiery explosive has been ignited!\""})
@Since("INSERT VERSION")
public class CondExplodesWithFire extends Condition {

	static {
		Skript.registerCondition(CondExplodesWithFire.class,
				"%entities% [(1¦((does|do) not|(doesn't|don't)))] explode[s] with fire",
				"%entities% [(1¦((does|do) not|(doesn't|don't)))] cause[s] (a fiery|an incendiary explosion)",
				"the [event(-| )]explosion (is|1¦(is not|isn't)) fiery");
	}

	@SuppressWarnings("null")
	private Expression<Entity> entities;

	private boolean isEvent;

	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		isEvent = matchedPattern == 2;
		if (isEvent) {
			if (!ScriptLoader.isCurrentEvent(ExplosionPrimeEvent.class)) {
				Skript.error("Checking if the event explosion is fiery is only possible in explosion prime events", ErrorQuality.SEMANTIC_ERROR);
				return false;
			}
		}
		if (matchedPattern < 2)
			entities = (Expression<Entity>) exprs[0];
		setNegated(parseResult.mark == 1);
		return true;
	}

	@Override
	public boolean check(Event e) {
		if (isEvent) {
			if (isNegated())
				return !((ExplosionPrimeEvent) e).getFire();
			return ((ExplosionPrimeEvent) e).getFire();
		}
		return entities.check(e, entity -> entity instanceof Explosive && ((Explosive) entity).isIncendiary(), isNegated());
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		if (isEvent)
			return "the event-explosion " + (isNegated() == false ? "" : "is not") + " fiery (ExplosionPrimeEvent)";
		return entities.toString(e, debug) + (isNegated() == false ? "" : " do not") + " explode with fire";
	}
}
