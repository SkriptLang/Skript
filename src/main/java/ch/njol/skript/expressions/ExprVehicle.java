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
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.entity.EntityMountEvent;
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
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * @author Peter Güttinger
 */
@Name("Vehicle")
@Description({"The vehicle an entity is in, if any. This can actually be any entity, e.g. spider jockeys are skeletons that ride on a spider, so the spider is the 'vehicle' of the skeleton.",
		"See also: <a href='#ExprPassenger'>passenger</a>"})
@Examples({"vehicle of the player is a minecart"})
@Since("2.0")
public class ExprVehicle extends SimplePropertyExpression<Entity, Entity> {

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
		register(ExprVehicle.class, Entity.class, "vehicle[s]", "entities");

		// legacy support
		boolean hasOldMountEvents = !HAS_NEW_MOUNT_EVENTS &&
				Skript.classExists("org.spigotmc.event.entity.EntityMountEvent");
		Class<?> oldMountEventClass = null;
		MethodHandle oldGetMountHandle = null;
		Class<?> oldDismountEventClass = null;
		MethodHandle oldGetDismountedHandle = null;
		if (hasOldMountEvents) {
			try {
				MethodHandles.Lookup lookup = MethodHandles.lookup();
				MethodType entityReturnType = MethodType.methodType(Entity.class);
				// mount event
				oldMountEventClass = Class.forName("org.spigotmc.event.entity.EntityMountEvent");
				oldGetMountHandle = lookup.findVirtual(oldMountEventClass, "getMount", entityReturnType);
				// dismount event
				oldDismountEventClass = Class.forName("org.spigotmc.event.entity.EntityDismountEvent");
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
	
	@Override
	protected Entity[] get(final Event e, final Entity[] source) {
		return get(source, entity -> {
			if (getTime() >= 0 && e instanceof VehicleEnterEvent && entity.equals(((VehicleEnterEvent) e).getEntered()) && !Delay.isDelayed(e)) {
				return ((VehicleEnterEvent) e).getVehicle();
			}
			if (getTime() >= 0 && e instanceof VehicleExitEvent && entity.equals(((VehicleExitEvent) e).getExited()) && !Delay.isDelayed(e)) {
				return ((VehicleExitEvent) e).getVehicle();
			}
			if (
				(HAS_OLD_MOUNT_EVENTS || HAS_NEW_MOUNT_EVENTS)
				&& getTime() >= 0 && !Delay.isDelayed(e)
				&& e instanceof EntityEvent && entity.equals(((EntityEvent) e).getEntity())
			) {
				if (HAS_NEW_MOUNT_EVENTS) {
					if (e instanceof EntityMountEvent)
						return ((EntityMountEvent) e).getMount();
					if (e instanceof EntityDismountEvent)
						return ((EntityDismountEvent) e).getDismounted();
				} else { // legacy mount event support
					try {
						assert OLD_MOUNT_EVENT_CLASS != null;
						if (OLD_MOUNT_EVENT_CLASS.isInstance(e)) {
							assert OLD_GETMOUNT_HANDLE != null;
							return (Entity) OLD_GETMOUNT_HANDLE.invoke(e);
						}
						assert OLD_DISMOUNT_EVENT_CLASS != null;
						if (OLD_DISMOUNT_EVENT_CLASS.isInstance(e)) {
							assert OLD_GETDISMOUNTED_HANDLE != null;
							return (Entity) OLD_GETDISMOUNTED_HANDLE.invoke(e);
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
	public Entity convert(final Entity e) {
		assert false;
		return e.getVehicle();
	}
	
	@Override
	public Class<? extends Entity> getReturnType() {
		return Entity.class;
	}
	
	@Override
	protected String getPropertyName() {
		return "vehicle";
	}
	
	@Override
	@Nullable
	public Class<?>[] acceptChange(final ChangeMode mode) {
		if (mode == ChangeMode.SET) {
			return new Class[] {Entity.class, EntityData.class};
		}
		return super.acceptChange(mode);
	}
	
	@Override
	public void change(final Event e, final @Nullable Object[] delta, final ChangeMode mode) {
		if (mode == ChangeMode.SET) {
			assert delta != null;
			final Entity[] ps = getExpr().getArray(e);
			if (ps.length == 0)
				return;
			final Object o = delta[0];
			if (o instanceof Entity) {
				((Entity) o).eject();
				final Entity p = CollectionUtils.getRandom(ps);
				assert p != null;
				p.leaveVehicle();
				((Entity) o).setPassenger(p);
			} else if (o instanceof EntityData) {
				for (final Entity p : ps) {
					final Entity v = ((EntityData<?>) o).spawn(p.getLocation());
					if (v == null)
						continue;
					v.setPassenger(p);
				}
			} else {
				assert false;
			}
		} else {
			super.change(e, delta, mode);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean setTime(final int time) {
		return super.setTime(time, getExpr(), VehicleEnterEvent.class, VehicleExitEvent.class);
	}
	
}
