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

import java.util.function.Predicate;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.expressions.base.SimplePropertyExpression;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.event.entity.EntityMountEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.jetbrains.annotations.Nullable;

@Name("Vehicle")
@Description({
	"The vehicle an entity is in, if any.",
	"This can actually be any entity, e.g. spider jockeys are skeletons that ride on a spider, so the spider is the 'vehicle' of the skeleton.",
	"See also: <a href='#ExprPassenger'>passenger</a>"
})
@Examples({
	"set the vehicle of {game::players::*} to a saddled pig",
	"give {game::players::*} a carrot on a stick",
	"",
	"on vehicle enter:",
		"\tvehicle is a horse",
		"\tadd 1 to {statistics::horseMounting::%uuid of player%}",
})
@Since("2.0")
public class ExprVehicle extends SimplePropertyExpression<Entity, Entity> {

	static {
		registerDefault(ExprVehicle.class, Entity.class, "vehicle[s]", "entities");
	}

	@Override
	protected Entity[] get(Event event, Entity[] source) {
		return get(source, entity -> {
			if (getTime() >= 0 && event instanceof VehicleEnterEvent vehicleEnterEvent && entity.equals(vehicleEnterEvent.getEntered())) {
				return vehicleEnterEvent.getVehicle();
			}
			if (getTime() <= 0 && event instanceof VehicleExitEvent vehicleExitEvent && entity.equals(vehicleExitEvent.getExited())) {
				return vehicleExitEvent.getVehicle();
			}
			if (getTime() >= 0 && event instanceof EntityMountEvent entityMountEvent && entity.equals(entityMountEvent.getEntity())) {
				return entityMountEvent.getMount();
			}
			if (getTime() <= 0 && event instanceof EntityDismountEvent entityDismountEvent && entity.equals(entityDismountEvent.getEntity())) {
				return entityDismountEvent.getDismounted();
			}
			return entity.getVehicle();
		});
	}

	@Override
	@Nullable
	public Entity convert(Entity entity) {
		return entity.getVehicle();
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET) {
			if (isDefault() && getParser().isCurrentEvent(VehicleExitEvent.class, EntityDismountEvent.class)) {
				Skript.error("Setting the vehicle during a dismount/exit vehicle event will create an infinite mounting loop.");
				return null;
			}
			return new Class[] {Entity.class, EntityData.class};
		}
		return super.acceptChange(mode);
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		if (mode == ChangeMode.SET) {
			// The player can desync if setting an entity as it's currently mounting it.
			// Remember that there can be other entity types aside from players, so only cancel this for players.
			Predicate<Entity> predicate = Player.class::isInstance;
			if (event instanceof EntityMountEvent entityMountEvent && predicate.test(entityMountEvent.getEntity())) {
				return;
			}
			if (event instanceof VehicleEnterEvent vehicleEnterEvent && predicate.test(vehicleEnterEvent.getEntered())) {
				return;
			}
			Entity[] passengers = getExpr().getArray(event);
			if (passengers.length == 0)
				return;
			assert delta != null;
			Object object = delta[0];
			if (object instanceof Entity) {
				Entity entity = (Entity) object;
				entity.eject();
				for (Entity passenger : passengers) {
					// Avoid infinity mounting
					if (event instanceof VehicleExitEvent && predicate.test(passenger) && passenger.equals(((VehicleExitEvent) event).getExited()))
						continue;
					if (event instanceof EntityDismountEvent && predicate.test(passenger) && passenger.equals(((EntityDismountEvent) event).getEntity()))
						continue;
					assert passenger != null;
					passenger.leaveVehicle();
					entity.addPassenger(passenger);
				}
			} else if (object instanceof EntityData) {
				EntityData<?> entityData = (EntityData<?>) object;
				VehicleExitEvent vehicleExitEvent = event instanceof VehicleExitEvent ? (VehicleExitEvent) event : null;
				EntityDismountEvent entityDismountEvent = event instanceof EntityDismountEvent ? (EntityDismountEvent) event : null;
				for (Entity passenger : passengers) {
					// Avoid infinity mounting
					if (vehicleExitEvent != null && predicate.test(passenger) && passenger.equals(vehicleExitEvent.getExited()))
						continue;
					if (entityDismountEvent != null && predicate.test(passenger) && passenger.equals(entityDismountEvent.getEntity()))
						continue;
					Entity vehicle = entityData.spawn(passenger.getLocation());
					if (vehicle == null)
						continue;
					vehicle.addPassenger(passenger);
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
		return super.setTime(time, getExpr(), VehicleEnterEvent.class, VehicleExitEvent.class, EntityMountEvent.class, EntityDismountEvent.class);
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
