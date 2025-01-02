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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Arrays;

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.Converter;

import ch.njol.skript.Skript;
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
@Since("2.0, 2.2-dev26")
public class ExprPassengers extends PropertyExpression<Entity, Entity> {

	private static final boolean HAS_NEW_MOUNT_EVENTS = Skript.classExists("org.bukkit.event.entity.EntityMountEvent");

	private static final boolean HAS_OLD_MOUNT_EVENTS;
	@Nullable
	private static final Class<?> OLD_MOUNT_EVENT_CLASS;
	@Nullable
	private static final MethodHandle OLD_GETMOUNT_HANDLE;
	@Nullable
	private static final Class<?> OLD_DISMOUNT_EVENT_CLASS;
	@Nullable
	private static final MethodHandle OLD_GETDISMOUNTED_HANDLE;

	static {
		registerDefault(ExprPassengers.class, Entity.class, "passenger[:s]", "entities");

		// legacy support. In 1.20 spigot moved this event from the package org.spigotmc.event to org.bukkit.event
		boolean hasOldMountEvents = !HAS_NEW_MOUNT_EVENTS &&
				Skript.classExists("org.spigotmc.event.entity.EntityMountEvent");
		Class<? extends Event> oldMountEventClass = null;
		MethodHandle oldGetMountHandle = null;
		Class<? extends Event> oldDismountEventClass = null;
		MethodHandle oldGetDismountedHandle = null;
		if (hasOldMountEvents) {
			try {
				MethodHandles.Lookup lookup = MethodHandles.lookup();
				MethodType entityReturnType = MethodType.methodType(Entity.class);
				// mount event
				oldMountEventClass = (Class<? extends Event>) Class.forName("org.spigotmc.event.entity.EntityMountEvent");
				oldGetMountHandle = lookup.findVirtual(oldMountEventClass, "getMount", entityReturnType);
				// dismount event
				oldDismountEventClass = (Class<? extends Event>) Class.forName("org.spigotmc.event.entity.EntityDismountEvent");
				oldGetDismountedHandle = lookup.findVirtual(oldDismountEventClass, "getDismounted", entityReturnType);
			} catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException e) {
				hasOldMountEvents = false;
				oldMountEventClass = null;
				oldGetMountHandle = null;
				oldDismountEventClass = null;
				oldGetDismountedHandle = null;
				Skript.exception(e, "Failed to load old mount event support.");
			}
		}
		HAS_OLD_MOUNT_EVENTS = hasOldMountEvents;
		OLD_MOUNT_EVENT_CLASS = oldMountEventClass;
		OLD_GETMOUNT_HANDLE = oldGetMountHandle;
		OLD_DISMOUNT_EVENT_CLASS = oldDismountEventClass;
		OLD_GETDISMOUNTED_HANDLE = oldGetDismountedHandle;
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
			if (getTime() != EventValues.TIME_PAST && event instanceof VehicleEnterEvent vehicleEnterEvent && entity.equals(vehicleEnterEvent.getVehicle()))
				return new Entity[] {vehicleEnterEvent.getEntered()};
			if (getTime() != EventValues.TIME_FUTURE && event instanceof VehicleExitEvent vehicleExitEvent && entity.equals(vehicleExitEvent.getVehicle()))
				return new Entity[] {vehicleExitEvent.getExited()};
			if (HAS_NEW_MOUNT_EVENTS) {
				if (getTime() != EventValues.TIME_PAST && event instanceof org.bukkit.event.entity.EntityMountEvent entityMountEvent && entity.equals(entityMountEvent.getEntity()))
					return new Entity[] {entityMountEvent.getEntity()};
				if (getTime() != EventValues.TIME_FUTURE && event instanceof org.bukkit.event.entity.EntityDismountEvent entityDismountEvent && entity.equals(entityDismountEvent.getEntity()))
					return new Entity[] {entityDismountEvent.getEntity()};
			} else if (HAS_OLD_MOUNT_EVENTS) {
				try {
					if (getTime() != EventValues.TIME_PAST && event.getClass().equals(OLD_MOUNT_EVENT_CLASS)) {
						Entity mount = (Entity) OLD_GETMOUNT_HANDLE.invoke(event);
						if (entity.equals(mount))
							return new Entity[] {mount};
					}
					if (getTime() != EventValues.TIME_FUTURE && event.getClass().equals(OLD_DISMOUNT_EVENT_CLASS)) {
						Entity mount = (Entity) OLD_GETDISMOUNTED_HANDLE.invoke(event);
						if (entity.equals(mount))
							return new Entity[] {mount};
					}
				} catch (Throwable e) {
					Skript.exception(e, "Failed to get passengers from old mount events.");
				}
			}
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
		return switch (mode) {
			case ADD, DELETE, REMOVE, REMOVE_ALL, RESET, SET -> CollectionUtils.array(Entity[].class, EntityData[].class);
			default -> null;
		};
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
				for (Object object : delta) {
					for (Entity vehicle : vehicles) {
						if (object instanceof Entity passenger) {
							vehicle.removePassenger(passenger);
						} else {
							for (Entity passenger : vehicle.getPassengers()) {
								if (passenger != null && ((EntityData<?>) object).isInstance((passenger)))
									vehicle.removePassenger(passenger);
							}
						}
					}
				}
				break;
			case REMOVE:
				for (Object object : delta) {
					for (Entity vehicle : vehicles) {
						if (object instanceof Entity passenger) {
							vehicle.removePassenger(passenger);
						} else {
							Entity passenger = CollectionUtils.getRandom(vehicle.getPassengers().stream()
								.filter(p -> ((EntityData<?>) object).isInstance(p))
								.toList());
							vehicle.removePassenger(passenger);
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
	@SuppressWarnings("unchecked")
	public boolean setTime(int time) {
		if (time == EventValues.TIME_PAST) {
			if (HAS_OLD_MOUNT_EVENTS)
				return super.setTime(time, getExpr(), (Class<? extends Event>[]) CollectionUtils.array(OLD_DISMOUNT_EVENT_CLASS, VehicleExitEvent.class));
			super.setTime(time, getExpr(), org.bukkit.event.entity.EntityDismountEvent.class, VehicleExitEvent.class);
		}
		if (time == EventValues.TIME_FUTURE) {
			if (HAS_OLD_MOUNT_EVENTS)
				return super.setTime(time, getExpr(), (Class<? extends Event>[]) CollectionUtils.array(OLD_MOUNT_EVENT_CLASS, VehicleEnterEvent.class));
			return super.setTime(time, getExpr(), org.bukkit.event.entity.EntityMountEvent.class, VehicleEnterEvent.class);
		}
		if (HAS_OLD_MOUNT_EVENTS)
			return super.setTime(time, getExpr(), (Class<? extends Event>[]) CollectionUtils.array(OLD_DISMOUNT_EVENT_CLASS, VehicleExitEvent.class, OLD_MOUNT_EVENT_CLASS, VehicleEnterEvent.class));
		return super.setTime(time, getExpr(), org.bukkit.event.entity.EntityDismountEvent.class, VehicleExitEvent.class, org.bukkit.event.entity.EntityMountEvent.class, VehicleEnterEvent.class);
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
