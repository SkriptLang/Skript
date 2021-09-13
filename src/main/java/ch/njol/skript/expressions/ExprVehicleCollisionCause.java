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
package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.event.vehicle.VehicleBlockCollisionEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.eclipse.jdt.annotation.Nullable;

@Name("Vehicle Collision Cause")
@Description("The vehicle collision cause. This will return either a block or an entity.")
@Examples({"on vehicle collision:",
	"\tif vehicle collision cause is an entity:",
	"\t\tkill event-entity",
	"",
	"on vehicle block collision:",
	"\tif collision cause is a block:",
	"\t\tdamage event-entity by 1 heart"})
@Since("INSERT VERSION")
public class ExprVehicleCollisionCause extends SimpleExpression<Object> {

	static {
		Skript.registerExpression(ExprVehicleCollisionCause.class, Object.class, ExpressionType.SIMPLE, "[the] [vehicle] collision cause");
	}
	
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parser) {
		if (!getParser().isCurrentEvent(VehicleEntityCollisionEvent.class, VehicleBlockCollisionEvent.class)) {
			Skript.error("Cannot use 'vehicle collision cause' outside of a vehicle collision event.");
			return false;
		}
		return true;
	}

	@Override
	protected Object[] get(Event e) {
		return new Object[] { e instanceof VehicleEntityCollisionEvent ? ((VehicleEntityCollisionEvent) e).getEntity() : ((VehicleBlockCollisionEvent) e).getBlock()};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Object> getReturnType() {
		return Object.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "vehicle collision cause";
	}

}
