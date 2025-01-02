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

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.EventValues;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

@Name("Vehicle")
@Description({"The vehicle an entity is in, if any. This can actually be any entity, e.g. spider jockeys are skeletons that ride on a spider, so the spider is the 'vehicle' of the skeleton.",
		"See also: <a href='#ExprPassenger'>passenger</a>"})
@Examples({"vehicle of the player is a minecart"})
@Since("2.0")
public class ExprVehicle extends PropertyExpression<Entity, Entity> {

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
		registerDefault(ExprVehicle.class, Entity.class, "vehicle[:s]", "entities");

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
		if (plural && getExpr().isDefault()) {
			Skript.error("An event cannot contain multiple vehicles. Use 'vehicle' with no plurality in vehicle events.");
			return false;
		}
		return true;
	}

	@Override
	protected Entity[] get(Event event, Entity[] source) {
		return get(source, entity -> {
			if (getTime() >= 0 && event instanceof VehicleEnterEvent vehicleEnterEvent && entity.equals(vehicleEnterEvent.getEntered()))
				return vehicleEnterEvent.getVehicle();
			if (getTime() >= 0 && event instanceof VehicleExitEvent vehicleExitEvent && entity.equals(vehicleExitEvent.getExited()))
				return vehicleExitEvent.getVehicle();
			if (
				(HAS_OLD_MOUNT_EVENTS || HAS_NEW_MOUNT_EVENTS)
				&& getTime() >= 0 && !Delay.isDelayed(event)
				&& event instanceof EntityEvent entityEvent && entity.equals(entityEvent.getEntity())
			) {
				if (HAS_NEW_MOUNT_EVENTS) {
					if (event instanceof org.bukkit.event.entity.EntityMountEvent entityMountEvent)
						return entityMountEvent.getMount();
					if (event instanceof org.bukkit.event.entity.EntityDismountEvent entityDismountEvent)
						return entityDismountEvent.getDismounted();
				} else { // legacy mount event support
					try {
						assert OLD_MOUNT_EVENT_CLASS != null;
						if (OLD_MOUNT_EVENT_CLASS.isInstance(event)) {
							assert OLD_GETMOUNT_HANDLE != null;
							return (Entity) OLD_GETMOUNT_HANDLE.invoke(event);
						}
						assert OLD_DISMOUNT_EVENT_CLASS != null;
						if (OLD_DISMOUNT_EVENT_CLASS.isInstance(event)) {
							assert OLD_GETDISMOUNTED_HANDLE != null;
							return (Entity) OLD_GETDISMOUNTED_HANDLE.invoke(event);
						}
					} catch (Throwable ex) {
						Skript.exception(ex, "An error occurred while trying to invoke legacy mount event support.");
					}
				}
			}
			return entity.getVehicle();
		});
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET) {
			if (isSingle())
				return CollectionUtils.array(Entity.class, EntityData.class);
			Skript.error("You may only set the vehicle of one entity at a time. " + 
					"The same vehicle cannot be applied to multiple entities. " +
					"Use the 'passengers of' expression if you wish to update multiple riders.");
			return null;
		}
		return super.acceptChange(mode);
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		if (mode == ChangeMode.SET) {
			assert delta != null;
			Entity passenger = getExpr().getSingle(event);
			if (passenger == null)
				return;
			Object object = delta[0];
			if (object instanceof Entity entity) {
				entity.eject();
				passenger.leaveVehicle();
				entity.addPassenger(passenger);
			} else if (object instanceof EntityData entityData) {
				Entity vehicle = entityData.spawn(passenger.getLocation());
				if (vehicle == null)
					return;
				vehicle.addPassenger(passenger);
			}
			return;
		}
		super.change(event, delta, mode);
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
		return "vehicle" + (plural ? "s " : " ") + "of " + getExpr().toString(event, debug);
	}

}
