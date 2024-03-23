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
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.eclipse.jdt.annotation.Nullable;
import org.spigotmc.event.entity.EntityDismountEvent;
import org.spigotmc.event.entity.EntityMountEvent;

@Name("Vehicle")
@Description({
	"The vehicle an entity is in, if any.",
	"This can actually be any entity, e.g. spider jockeys are skeletons that ride on a spider, so the spider is the 'vehicle' of the skeleton.",
	"See also: <a href='#ExprPassenger'>passenger</a>"
})
@Examples({"vehicle of the player is a minecart"})
@Since("2.0")
public class ExprVehicle extends SimplePropertyExpression<Entity, Entity> {

	private static final boolean hasMountEvents = Skript.classExists("org.spigotmc.event.entity.EntityMountEvent");

	static {
		registerDefault(ExprVehicle.class, Entity.class, "vehicle[s]", "entities");
	}

	@Override
	protected Entity[] get(Event event, Entity[] source) {
		return get(source, entity -> {
			if (getTime() >= 0 && event instanceof VehicleEnterEvent && entity.equals(((VehicleEnterEvent) event).getEntered()) && !Delay.isDelayed(event)) {
				return ((VehicleEnterEvent) event).getVehicle();
			}
			if (getTime() >= 0 && event instanceof VehicleExitEvent && entity.equals(((VehicleExitEvent) event).getExited()) && !Delay.isDelayed(event)) {
				return ((VehicleExitEvent) event).getVehicle();
			}
			if (hasMountEvents) {
				if (getTime() >= 0 && event instanceof EntityMountEvent && entity.equals(((EntityMountEvent) event).getEntity()) && !Delay.isDelayed(event)) {
					return ((EntityMountEvent) event).getMount();
				}
				if (getTime() >= 0 && event instanceof EntityDismountEvent && entity.equals(((EntityDismountEvent) event).getEntity()) && !Delay.isDelayed(event)) {
					return ((EntityDismountEvent) event).getDismounted();
				}
			}
			return entity.getVehicle();
		});
	}

	@Override
	@Nullable
	public Entity convert(Entity entity) {
		assert false;
		return entity.getVehicle();
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET) 
			return new Class[] {Entity.class, EntityData.class};
		return super.acceptChange(mode);
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		if (mode == ChangeMode.SET) {
			assert delta != null;
			Entity[] passengers = getExpr().getArray(event);
			if (passengers.length == 0)
				return;
			Object object = delta[0];
			if (object instanceof Entity) {
				((Entity) object).eject();
				Entity passenger = CollectionUtils.getRandom(passengers);
				assert passenger != null;
				passenger.leaveVehicle();
				((Entity) object).setPassenger(passenger);
			} else if (object instanceof EntityData) {
				for (Entity passenger : passengers) {
					Entity vehicle = ((EntityData<?>) object).spawn(passenger.getLocation());
					if (vehicle == null)
						continue;
					vehicle.setPassenger(passenger);
				}
			} else {
				assert false;
			}
		} else {
			super.change(event, delta, mode);
		}
	}

	@Override
	public boolean setTime(int time) {
		return super.setTime(time, getExpr(), VehicleEnterEvent.class, VehicleExitEvent.class);
	}

	@Override
	public Class<? extends Entity> getReturnType() {
		return Entity.class;
	}

	@Override
	protected String getPropertyName() {
		return "vehicle";
	}

}
