package ch.njol.skript.expressions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.PassengerUtils;
import ch.njol.skript.classes.Changer.ChangeMode;
import org.skriptlang.skript.lang.converter.Converter;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@Name("Passenger")
@Description({"The passenger of a vehicle, or the rider of a mob.",
		"For 1.11.2 and above, it returns a list of passengers and you can use all changers in it.",
		"See also: <a href='#ExprVehicle'>vehicle</a>"})
@Examples({"#for 1.11 and lower",
		"passenger of the minecart is a creeper or a cow",
		"the saddled pig's passenger is a player",
		"#for 1.11.2+",
		"passengers of the minecart contains a creeper or a cow",
		"the boat's passenger contains a pig",
		"add a cow and a zombie to passengers of last spawned boat",
		"set passengers of player's vehicle to a pig and a horse",
		"remove all pigs from player's vehicle",
		"clear passengers of boat"})
@Since("2.0, 2.2-dev26 (Multiple passengers for 1.11.2+)")
public class ExprPassenger extends SimpleExpression<Entity> { // REMIND create 'vehicle' and 'passenger' expressions for vehicle enter/exit events?
	static { // It was necessary to convert to SimpleExpression due to the method 'isSingle()'.
		Skript.registerExpression(ExprPassenger.class, Entity.class, ExpressionType.PROPERTY, "[the] passenger[s] of %entities%", "%entities%'[s] passenger[s]");
	}
	
	@SuppressWarnings("null")
	private Expression<Entity> vehicle;
	
	@Override
	@Nullable
	protected Entity[] get(Event e) {
		Entity[] source = vehicle.getAll(e);
		Converter<Entity, Entity[]> conv = new Converter<Entity, Entity[]>(){
			@Override
			@Nullable
			public Entity[] convert(Entity v) {
				if (getTime() >= 0 && e instanceof VehicleEnterEvent && v.equals(((VehicleEnterEvent) e).getVehicle()) && !Delay.isDelayed(e)) {
					return new Entity[] {((VehicleEnterEvent) e).getEntered()};
				}
				if (getTime() >= 0 && e instanceof VehicleExitEvent && v.equals(((VehicleExitEvent) e).getVehicle()) && !Delay.isDelayed(e)) {
					return new Entity[] {((VehicleExitEvent) e).getExited()};
				}
				return PassengerUtils.getPassenger(v);
			}};
			
		List<Entity> entities = new ArrayList<>();
		for (Entity v : source) {
			if (v == null)
				continue;
			Entity[] array = conv.convert(v);
			if (array != null && array.length > 0)
				entities.addAll(Arrays.asList(array));
		}
		return entities.toArray(new Entity[entities.size()]);
	}
	
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		vehicle = (Expression<Entity>) exprs[0];
		return true;
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(final ChangeMode mode) {
		if (!isSingle())
			return new Class[] {Entity[].class, EntityData[].class}; // To support more than one entity
		if (mode == ChangeMode.SET)
			return new Class[] {Entity.class, EntityData.class};
		return super.acceptChange(mode);
	}

	@SuppressWarnings("null")
	@Override
	public void change(final Event e, final @Nullable Object[] delta, final ChangeMode mode) {
		Entity[] vehicles = this.vehicle.getArray(e);
		if (!isSingle() || mode == ChangeMode.SET) {
			for (Entity vehicle: vehicles){
				if (vehicle == null)
					continue;
				switch(mode){
					case SET: 
						vehicle.eject();
						//$FALL-THROUGH$
					case ADD:
						if (delta == null || delta.length == 0)
							return;
						for (Object obj : delta){
							if (obj == null)
								continue;
							Entity passenger = obj instanceof Entity ? (Entity)obj: ((EntityData<?>)obj).spawn(vehicle.getLocation());
							PassengerUtils.addPassenger(vehicle, passenger);
						}
						break;
					case REMOVE_ALL:
					case REMOVE:
						if (delta == null || delta.length == 0)
							return;
						for (Object obj : delta){
							if (obj == null)
								continue;
							if (obj instanceof Entity){
								PassengerUtils.removePassenger(vehicle, (Entity)obj);
							} else {
								for (Entity passenger : PassengerUtils.getPassenger(vehicle))
									if (passenger != null && ((EntityData<?>)obj).isInstance((passenger))){
										PassengerUtils.removePassenger(vehicle, passenger);
									}
							}
						}
						break;
					case RESET:
					case DELETE:
						vehicle.eject();
				}
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
	public String toString(@Nullable Event e, boolean debug) {
		return "the passenger of " + vehicle.toString(e, debug);
	}
	
	@Override
	public boolean isSingle() {
		// In case it doesn't have multiple passenger support, it's up to the source expression to determine if it's single, otherwise is always false
		return !PassengerUtils.hasMultiplePassenger() ? vehicle.isSingle() : false;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean setTime(final int time) {
		return super.setTime(time, vehicle, VehicleEnterEvent.class, VehicleExitEvent.class);
	}	
}
