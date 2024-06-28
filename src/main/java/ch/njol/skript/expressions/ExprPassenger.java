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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.classes.Changer.ChangeMode;
import org.skriptlang.skript.lang.converter.Converter;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@Name("Passengers")
@Description({"The passengers of a vehicle, or the riders of a mob.",
		"This returns a list of passengers and you can use all changers in it.",
		"See also: <a href='#ExprVehicle'>vehicle</a>"})
@Examples({
		"passengers of the minecart contains a creeper or a cow",
		"the boat's passenger contains a pig",
		"add a cow and a zombie to passengers of last spawned boat",
		"set passengers of player's vehicle to a pig and a horse",
		"remove all pigs from player's vehicle",
		"clear passengers of boat"})
@Since("2.0, 2.2-dev26 (Multiple passengers)")
public class ExprPassenger extends PropertyExpression<Entity, Entity> { // TODO create 'vehicle' and 'passenger' expressions for vehicle enter/exit events?

	static {
		register(ExprPassenger.class, Entity.class, "passenger[s]", "entities");
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr((Expression<Entity>) exprs[0]);
		return true;
	}
	
	@Override
	@Nullable
	protected Entity[] get(Event event, Entity[] source) {
		Converter<Entity, Entity[]> converter = new Converter<Entity, Entity[]>(){
			@Override
			@Nullable
			public Entity[] convert(Entity vehicle) {
				if (getTime() >= 0 && event instanceof VehicleEnterEvent && vehicle.equals(((VehicleEnterEvent) event).getVehicle()) && !Delay.isDelayed(event))
					return new Entity[]{((VehicleEnterEvent) event).getEntered()};
				if (getTime() >= 0 && event instanceof VehicleExitEvent && vehicle.equals(((VehicleExitEvent) event).getVehicle()) && !Delay.isDelayed(event))
					return new Entity[]{((VehicleExitEvent) event).getExited()};
				return vehicle.getPassengers().toArray(new Entity[0]);
			}
		};

		List<Entity> totalPassengers = new ArrayList<>();
		for (Entity vehicle : source) {
			if (vehicle == null)
				continue;
			Entity[] passengers = converter.convert(vehicle);
			if (passengers != null && passengers.length > 0)
				totalPassengers.addAll(Arrays.asList(passengers));
		}
		return totalPassengers.toArray(new Entity[0]);
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		return CollectionUtils.array(Entity[].class, EntityData[].class);
	}

	@SuppressWarnings("null")
	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		for (Entity vehicle : getExpr().getArray(event)) {
			if (vehicle == null)
				continue;

			switch (mode) {
				case SET:
					vehicle.eject();
					// falls through
				case ADD:
					if (delta == null || delta.length == 0)
						return;
					for (Object object : delta) {
						if (object == null)
							continue;
						Entity passenger = object instanceof Entity ? (Entity) object : ((EntityData<?>) object).spawn(vehicle.getLocation());
						vehicle.addPassenger(passenger);
					}
					break;
				case REMOVE_ALL:
				case REMOVE:
					if (delta == null || delta.length == 0)
						return;
					for (Object object : delta) {
						if (object == null)
							continue;
						if (object instanceof Entity) {
							vehicle.removePassenger((Entity) object);
						} else {
							for (Entity passenger : vehicle.getPassengers()) {
								if (passenger != null && ((EntityData<?>) object).isInstance((passenger)))
									vehicle.removePassenger(passenger);
							}
						}
					}
					break;
				case RESET:
				case DELETE:
					vehicle.eject();
			}
		}
	}

	@Override
	public boolean setTime(int time) {
		return super.setTime(time, getExpr(), VehicleEnterEvent.class, VehicleExitEvent.class);
	}

	@Override
	public boolean isSingle() {
		return false;
	}
	
	@Override
	public Class<? extends Entity> getReturnType() {
		return Entity.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the passengers of " + getExpr().toString(event, debug);
	}

}
