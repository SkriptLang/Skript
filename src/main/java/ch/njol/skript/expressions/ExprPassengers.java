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

import java.util.Arrays;

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.eclipse.jdt.annotation.Nullable;
import org.skriptlang.skript.lang.converter.Converter;
import org.spigotmc.event.entity.EntityDismountEvent;
import org.spigotmc.event.entity.EntityMountEvent;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.EventValues;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

@Name("Passengers")
@Description({
	"The passengers of a vehicle, or the riders of a mob.",
	"You can use all changers in it.",
	"See also: <a href='#ExprVehicle'>vehicle</a>"
})
@Examples({
	"passengers of the minecart contains a creeper or a cow",
	"the boat's passenger contains a pig",
	"add a cow and a zombie to passengers of last spawned boat",
	"set passengers of player's vehicle to a pig and a horse",
	"remove all pigs from player's vehicle",
	"clear passengers of boat"
})
@Since("2.0, 2.2-dev26 (multiple passengers)")
public class ExprPassengers extends PropertyExpression<Entity, Entity> {

	static {
		registerDefault(ExprPassengers.class, Entity.class, "passenger[:s]", "entities");
	}

	private boolean plural;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr((Expression<Entity>) exprs[0]);
		plural = parseResult.hasTag("s");
		return true;
	}

	@Override
	protected Entity[] get(Event event, Entity[] source) {
		Converter<Entity, Entity[]> converter = entity -> {
			if (getTime() != EventValues.TIME_PAST && event instanceof VehicleEnterEvent && entity.equals(((VehicleEnterEvent) event).getVehicle()))
				return new Entity[] {((VehicleEnterEvent) event).getEntered()};
			if (getTime() != EventValues.TIME_FUTURE && event instanceof VehicleExitEvent && entity.equals(((VehicleExitEvent) event).getVehicle()))
				return new Entity[] {((VehicleExitEvent) event).getExited()};
			if (getTime() != EventValues.TIME_PAST && event instanceof EntityMountEvent && entity.equals(((EntityMountEvent) event).getEntity()))
				return new Entity[] {((EntityMountEvent) event).getEntity()};
			if (getTime() != EventValues.TIME_FUTURE && event instanceof EntityDismountEvent && entity.equals(((EntityDismountEvent) event).getEntity()))
				return new Entity[] {((EntityDismountEvent) event).getEntity()};
			return entity.getPassengers().toArray(new Entity[0]);
		};
		return Arrays.stream(source)
				.map(converter::convert)
				.flatMap(Arrays::stream)
				.toArray(Entity[]::new);
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		switch (mode) {
			case ADD:
			case DELETE:
			case REMOVE:
			case REMOVE_ALL:
			case RESET:
			case SET:
				return CollectionUtils.array(Entity[].class, EntityData[].class);
			default:
				return null;
		}
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		Entity[] vehicles = getExpr().getArray(event);
		switch (mode) {
			case SET:
				for (Entity vehicle : vehicles)
					vehicle.eject();
				//$FALL-THROUGH$
			case ADD:
				for (Object object : delta) {
					for (Entity vehicle : vehicles) {
						Entity passenger = object instanceof Entity ? (Entity) object : ((EntityData<?>) object).spawn(vehicle.getLocation());
						vehicle.addPassenger(passenger);
					}
				}
				break;
			case REMOVE_ALL:
			case REMOVE:
				for (Object object : delta) {
					for (Entity vehicle : vehicles) {
						if (object instanceof Entity) {
							vehicle.removePassenger((Entity) object);
						} else {
							for (Entity passenger : vehicle.getPassengers()) {
								if (passenger != null && ((EntityData<?>) object).isInstance((passenger)))
									vehicle.removePassenger(passenger);
							}
						}
					}
				}
				break;
			case DELETE:
			case RESET:
				for (Entity vehicle : vehicles)
					vehicle.eject();
				break;
			default:
				break;
		}
	}

	@Override
	public boolean isSingle() {
		return !plural && getExpr().isSingle();
	}

	@Override
	public boolean setTime(int time) {
		if (time == EventValues.TIME_PAST)
			super.setTime(time, getExpr(), EntityDismountEvent.class, VehicleExitEvent.class);
		if (time == EventValues.TIME_FUTURE)
			return super.setTime(time, getExpr(), EntityMountEvent.class, VehicleEnterEvent.class);
		return super.setTime(time, getExpr(), EntityDismountEvent.class, VehicleExitEvent.class, EntityMountEvent.class, VehicleEnterEvent.class);
	}

	@Override
	public Class<? extends Entity> getReturnType() {
		return Entity.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "passenger" + (plural ? "s " : " ") + "of " + getExpr().toString(event, debug);
	}

}
