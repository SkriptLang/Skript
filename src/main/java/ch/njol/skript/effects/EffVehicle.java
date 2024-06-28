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

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@Name("Vehicle")
@Description({"Makes an entity ride another entity, e.g. a minecart, a saddled pig, an arrow, etc."})
@Examples({"make the player ride a saddled pig",
		"make the attacker ride the victim"})
@Since("2.0")
public class EffVehicle extends Effect {
	static {
		Skript.registerEffect(EffVehicle.class,
				"(make|let|force) %entities% [to] (ride|mount) [in|on] %entities/entitydatas%",
				"(make|let|force) %entities% [to] (dismount|(dismount|leave) [from|of] [any|the[ir]|his|her] vehicle[s])",
				"(eject|dismount) [any|the] passenger[s] (of|from) %entities%");
	}

	@Nullable
	private Expression<Entity> passengers;
	@Nullable
	private Expression<?> vehicles;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		passengers = matchedPattern == 2 ? null : (Expression<Entity>) exprs[0];
		vehicles = matchedPattern == 1 ? null : exprs[exprs.length - 1];
		return true;
	}
	
	@Override
	protected void execute(Event e) {
		Expression<?> vehicles = this.vehicles;
		Expression<Entity> passengers = this.passengers;
		if (vehicles == null) {
			assert passengers != null;
			for (Entity passenger : passengers.getArray(e))
				passenger.leaveVehicle();
			return;
		}
		if (passengers == null) {
			assert vehicles != null;
			for (Object vehicle : vehicles.getArray(e))
				((Entity) vehicle).eject();
			return;
		}
		Object[] vehiclesArray = vehicles.getArray(e);
		if (vehiclesArray.length == 0)
			return;
		Entity[] passengersArray = passengers.getArray(e);
		if (passengersArray.length == 0)
			return;
		for (Object vehicle : vehiclesArray) {
			if (vehicle instanceof Entity) {
				((Entity) vehicle).eject();
				for (Entity passenger : passengersArray){
					assert passenger != null;
					passenger.leaveVehicle();
					((Entity) vehicle).addPassenger(passenger);
				}
			} else {
				for (Entity passenger : passengersArray) {
					assert passenger != null : passengers;
					Entity entity = ((EntityData<?>) vehicle).spawn(passenger.getLocation());
					if (entity == null)
						return;
					entity.addPassenger(passenger);
				}
			}
		}
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		Expression<?> vehicles = this.vehicles;
		Expression<Entity> passengers = this.passengers;
		if (vehicles == null) {
			assert passengers != null;
			return "make " + passengers.toString(e, debug) + " dismount";
		}
		if (passengers == null) {
			assert vehicles != null;
			return "eject passenger" + (vehicles.isSingle() ? "" : "s") + " of " + vehicles.toString(e, debug);
		}
		return "make " + passengers.toString(e, debug) + " ride " + vehicles.toString(e, debug);
	}
}
