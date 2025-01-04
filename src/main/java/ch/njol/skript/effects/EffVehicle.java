package ch.njol.skript.effects;

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.PassengerUtils;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@Name("Vehicle")
@Description("Makes an entity ride another entity, e.g. a minecart, a saddled pig, an arrow, etc.")
@Examples({
	"make the player ride a saddled pig",
	"make the attacker ride the victim"
})
@Since("2.0")
public class EffVehicle extends Effect {

	static {
		Skript.registerEffect(EffVehicle.class,
				"(make|let|force) %entities% [to] (ride|mount) [(in|on)] %"+ (PassengerUtils.hasMultiplePassenger() ? "entities" : "entity") +"/entitydatas%",
				"(make|let|force) %entities% [to] (dismount|(dismount|leave) (from|of|) (any|the[ir]|his|her|) vehicle[s])",
				"(eject|dismount) (any|the|) passenger[s] (of|from) %entities%");
	}

	private @Nullable Expression<Entity> passengers;
	private @Nullable Expression<?> vehicles;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		passengers = matchedPattern == 2 ? null : (Expression<Entity>) exprs[0];
		vehicles = matchedPattern == 1 ? null : exprs[exprs.length - 1];
		if (!PassengerUtils.hasMultiplePassenger() && passengers != null && vehicles != null && !passengers.isSingle() && vehicles.isSingle() && Entity.class.isAssignableFrom(vehicles.getReturnType()))
			Skript.warning("An entity can only have one passenger");
		return true;
	}
	
	@Override
	protected void execute(Event event) {
		// matchedPattern = 1
		if (vehicles == null) {
			assert passengers != null;
			for (Entity passenger : passengers.getArray(event))
				passenger.leaveVehicle();
			return;
		}
		// matchedPattern = 2
		if (passengers == null) {
			assert vehicles != null;
			for (Object vehicle : vehicles.getArray(event))
				((Entity) vehicle).eject();
			return;
		}
		// matchedPattern = 0
		Object[] vehiclesArray = vehicles.getArray(event);
		if (vehiclesArray.length == 0)
			return;
		Entity[] passengersArray = passengers.getArray(event);
		if (passengersArray.length == 0)
			return;
		for (Object vehicle : vehiclesArray) {
			if (vehicle instanceof Entity vehicleEntity) {
				vehicleEntity.eject();
				for (Entity passenger : passengersArray) {
					assert passenger != null;
					if (passenger == vehicleEntity)
						continue;
					passenger.leaveVehicle();
					PassengerUtils.addPassenger((Entity)vehicle, passenger); //For 1.9 and lower, it will only set the last one.
				}
			} else {
				for (Entity passenger : passengersArray) {
					assert passenger != null : passengers;
					Entity entity = ((EntityData<?>) vehicle).spawn(passenger.getLocation());
					if (entity == null)
						return;
					PassengerUtils.addPassenger(entity, passenger);
				}
			}
		}
	}
	
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (vehicles == null) {
			assert passengers != null;
			return "make " + passengers.toString(event, debug) + " dismount";
		}
		if (passengers == null) {
			assert vehicles != null;
			return "eject passenger" + (vehicles.isSingle() ? "" : "s") + " of " + vehicles.toString(event, debug);
		}
		return "make " + passengers.toString(event, debug) + " ride " + vehicles.toString(event, debug);
	}
	
}
