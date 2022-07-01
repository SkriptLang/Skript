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
import ch.njol.skript.classes.Converter;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;

@Name("Vehicle Of")
@Description({
	"The vehicle an entity is in, if any. This can actually be any entity, e.g. spider jockeys are skeletons that" +
		" ride on a spider, so the spider is the 'vehicle' of the skeleton.",
		"See also: <a href='./expressions.html#ExprPassenger'>passenger</a>"
})
@Examples("vehicle of the player is a minecart")
@Since("2.0")
public class ExprVehicleOf extends SimplePropertyExpression<Entity, Entity> {
	
	static boolean hasMountEvents = Skript.classExists("org.spigotmc.event.entity.EntityMountEvent");
	
	static {
		register(ExprVehicleOf.class, Entity.class, "vehicle[s]", "entities");
	}
	
	@Override
	protected Entity[] get(Event e, Entity[] source) {
		return get(source, new Converter<Entity, Entity>() {
			@Override
			@Nullable
			public Entity convert(Entity p) {
				if (getTime() >= 0 && e instanceof VehicleEnterEvent && p.equals(((VehicleEnterEvent) e).getEntered()) && !Delay.isDelayed(e)) {
					return ((VehicleEnterEvent) e).getVehicle();
				}
				if (getTime() >= 0 && e instanceof VehicleExitEvent && p.equals(((VehicleExitEvent) e).getExited()) && !Delay.isDelayed(e)) {
					return ((VehicleExitEvent) e).getVehicle();
				}
				if (hasMountEvents) {
					if (getTime() >= 0 && e instanceof EntityMountEvent && p.equals(((EntityMountEvent) e).getEntity()) && !Delay.isDelayed(e)) {
						return ((EntityMountEvent) e).getMount();
					}
					if (getTime() >= 0 && e instanceof EntityDismountEvent && p.equals(((EntityDismountEvent) e).getEntity()) && !Delay.isDelayed(e)) {
						return ((EntityDismountEvent) e).getDismounted();
					}
				}
				return p.getVehicle();
			}
		});
	}
	
	@Override
	@Nullable
	public Entity convert(Entity e) {
		return e.getVehicle();
	}
	
	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET) {
			return new Class[] {Entity.class, EntityData.class};
		}
		return super.acceptChange(mode);
	}
	
	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		if (mode == ChangeMode.SET) {
			assert delta != null;
			Entity[] ps = getExpr().getArray(e);
			if (ps.length == 0)
				return;
			Object o = delta[0];
			if (o instanceof Entity) {
				((Entity) o).eject();
				Entity p = CollectionUtils.getRandom(ps);
				assert p != null;
				p.leaveVehicle();
				((Entity) o).setPassenger(p);
			} else if (o instanceof EntityData) {
				for (Entity p : ps) {
					Entity v = ((EntityData<?>) o).spawn(p.getLocation());
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

	@Override
	public Class<? extends Entity> getReturnType() {
		return Entity.class;
	}

	@Override
	protected String getPropertyName() {
		return "vehicle";
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public boolean setTime(int time) {
		return super.setTime(time, getExpr(), VehicleEnterEvent.class, VehicleExitEvent.class);
	}
	
}
