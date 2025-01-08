package ch.njol.skript.effects;

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
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Vehicle")
@Description("Makes an entity ride another entity,")
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
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		passengers = matchedPattern == 2 ? null : (Expression<Entity>) exprs[0];
		vehicles = matchedPattern == 1 ? null : exprs[exprs.length - 1];
		if (!PassengerUtils.hasMultiplePassenger() && passengers != null && vehicles != null && !passengers.isSingle() && vehicles.isSingle() && Entity.class.isAssignableFrom(vehicles.getReturnType()))
			Skript.warning("An entity can only have one passenger");
		return true;
	}
	
	@Override
	protected void execute(Event event) {
		if (vehicles == null) {
			assert passengers != null;
			for (Entity passenger : passengers.getArray(event))
				passenger.leaveVehicle();
			return;
		}
		if (passengers == null) {
			assert vehicles != null;
			for (Object vehicle : vehicles.getArray(event))
				((Entity) vehicle).eject();
			return;
		}
		Object[] vehicles = this.vehicles.getArray(event);
		if (vehicles.length == 0)
			return;
		Entity[] passengers = this.passengers.getArray(event);
		if (passengers.length == 0)
			return;

		for (Object vehicle : vehicles) {
			if (vehicle instanceof Entity entity) {
				entity.eject();
				for (Entity passenger : passengers) {
					assert passenger != null;
					passenger.leaveVehicle();
					PassengerUtils.addPassenger(entity, passenger); //For 1.9 and lower, it will only set the last one.
				}
			} else {
				for (Entity passenger : passengers) {
					assert passenger != null : this.passengers;
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
		Expression<?> vehicles = this.vehicles;
		Expression<Entity> passengers = this.passengers;
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
