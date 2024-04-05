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
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.eclipse.jdt.annotation.Nullable;
import org.spigotmc.event.entity.EntityDismountEvent;
import org.spigotmc.event.entity.EntityMountEvent;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import org.skriptlang.skript.lang.converter.Converter;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;

/**
 * @author Peter Güttinger
 */
@Name("Vehicle")
@Description({"The vehicle an entity is in, if any. This can actually be any entity, e.g. spider jockeys are skeletons that ride on a spider, so the spider is the 'vehicle' of the skeleton.",
		"See also: <a href='#ExprPassenger'>passenger</a>"})
@Examples({"vehicle of the player is a minecart"})
@Since("2.0")
public class ExprVehicle extends SimplePropertyExpression<Entity, Entity> {
	
	static final boolean hasMountEvents = Skript.classExists("org.spigotmc.event.entity.EntityMountEvent");
	
	static {
		register(ExprVehicle.class, Entity.class, "vehicle[s]", "entities");
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
			if (hasMountEvents) {
				if (getTime() >= 0 && e instanceof EntityMountEvent && entity.equals(((EntityMountEvent) e).getEntity()) && !Delay.isDelayed(e)) {
					return ((EntityMountEvent) e).getMount();
				}
				if (getTime() >= 0 && e instanceof EntityDismountEvent && entity.equals(((EntityDismountEvent) e).getEntity()) && !Delay.isDelayed(e)) {
					return ((EntityDismountEvent) e).getDismounted();
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
