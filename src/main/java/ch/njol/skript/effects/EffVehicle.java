package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

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
				"(make|let|force) %entities% [to] (ride|mount) [(in|on)] %entity/entitydata%",
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
			for (Object vehicle : vehicles.getArray(event))
				((Entity) vehicle).eject();
			return;
		}
		// matchedPattern = 0
		Entity[] passengersArray = passengers.getArray(event);
		if (passengersArray.length == 0)
			return;
		Object vehicleObject = vehicles.getSingle(event);
		if (vehicleObject instanceof Entity vehicleEntity) {
			for (Entity passenger : passengersArray) {
				assert passenger != null;
				if (passenger == vehicleEntity)
					continue;
				passenger.leaveVehicle();
				vehicleEntity.addPassenger(passenger);
			}
		} else if (vehicleObject instanceof EntityData<?> vehicleData) {
			for (Entity passenger : passengersArray) {
				assert passenger != null;
				Entity vehicleEntity = vehicleData.spawn(passenger.getLocation());
				if (vehicleEntity == null)
					return;
				passenger.leaveVehicle();
				vehicleEntity.addPassenger(passenger);
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
			return "eject passenger" + (vehicles.isSingle() ? "" : "s") + " of " + vehicles.toString(event, debug);
		}
		return "make " + passengers.toString(event, debug) + " ride " + vehicles.toString(event, debug);
	}
	
}
