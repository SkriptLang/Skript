package ch.njol.skript.expressions;

import java.util.function.Predicate;

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.event.entity.EntityMountEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.EventValues;
import ch.njol.util.Kleenean;

import org.bukkit.entity.Player;

@Name("Vehicle")
@Description({
	"The vehicle an entity is in, if any.",
	"This can actually be any entity, e.g. spider jockeys are skeletons that ride on a spider, so the spider is the 'vehicle' of the skeleton.",
	"See also: <a href='#ExprPassenger'>passenger</a>"
})
@Example("""
	set the vehicle of {game::players::*} to a saddled pig
	give {game::players::*} a carrot on a stick
	""")
@Example("""
	on vehicle enter:
		vehicle is a horse
		add 1 to {statistics::horseMounting::%uuid of player%}
	""")
@Since("2.0")
public class ExprVehicle extends PropertyExpression<Entity, Entity> {

	static {
		if (Skript.classExists("org.bukkit.event.entity.EntityMountEvent"))
			registerDefault(ExprVehicle.class, Entity.class, "vehicle[s]", "entities");
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr((Expression<Entity>) expressions[0]);
		return true;
	}

	@Override
	protected Entity[] get(Event event, Entity[] source) {
		if (event instanceof EntityDismountEvent entityDismountEvent && getTime() != EventValues.TIME_FUTURE) {
			return get(source, e -> e.equals(entityDismountEvent.getEntity()) ? entityDismountEvent.getDismounted() : e.getVehicle());
		} else if (event instanceof VehicleEnterEvent vehicleEnterEvent && getTime() != EventValues.TIME_PAST) {
			return get(source, e -> e.equals(vehicleEnterEvent.getEntered()) ? vehicleEnterEvent.getVehicle() : e.getVehicle());
		} else if (event instanceof VehicleExitEvent vehicleExitEvent && getTime() != EventValues.TIME_FUTURE) {
			return get(source, e -> e.equals(vehicleExitEvent.getExited()) ? vehicleExitEvent.getVehicle() : e.getVehicle());
		} else if (event instanceof EntityMountEvent entityMountEvent && getTime() != EventValues.TIME_PAST) {
			return get(source, e -> e.equals(entityMountEvent.getEntity()) ? entityMountEvent.getMount() : e.getVehicle());
		} else {
			return get(source, Entity::getVehicle);
		}
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
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
			if (object instanceof Entity entity) {
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
			} else if (object instanceof EntityData entityData) {
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
	public String toString(@Nullable Event event, boolean debug) {
		return "vehicle of " + getExpr().toString(event, debug);
	}

}
