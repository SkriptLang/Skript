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
import org.bukkit.entity.Vehicle;
import org.bukkit.event.Event;
import org.bukkit.event.vehicle.VehicleEvent;
import org.eclipse.jdt.annotation.Nullable;

@Name("Vehicle")
@Description("The vehicle in a vehicle event.")
@Examples({
	"on vehicle damage:",
	"\tif the vehicle is a boat:",
	"\t\tcancel event"
})
@Since("INSERT VERSION")
public class ExprVehicle extends SimpleExpression<Vehicle> {

	static {
		Skript.registerExpression(ExprVehicle.class, Vehicle.class, ExpressionType.SIMPLE, "[the] vehicle");
	}
	
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parser) {
//		if (!VehicleEvent.class.isAssignableFrom(getParser().getCurrentEvents()[0])) {
		if (!getParser().isCurrentEvent(VehicleEvent.class)) { // does this work as above? still need to test
			Skript.error("Cannot use 'vehicle' outside of a vehicle related events");
			return false;
		}
		return true;
	}

	@Override
	protected Vehicle[] get(Event e) {
		return new Vehicle[] {((VehicleEvent) e).getVehicle()};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Vehicle> getReturnType() {
		return Vehicle.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "vehicle";
	}

}
