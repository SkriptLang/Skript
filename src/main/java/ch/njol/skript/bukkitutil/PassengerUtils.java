package ch.njol.skript.bukkitutil;

import java.lang.reflect.Method;

import org.bukkit.entity.Entity;

import ch.njol.skript.Skript;

/**
 * @author Peter Güttinger and contributors
 */
@SuppressWarnings("null")
public abstract class PassengerUtils {
	
	private PassengerUtils() {}
	
	//Using reflection methods cause it will be removed soon in 1.12
	private static Method getPassenger = null;
	private static Method setPassenger = null;
	
	
	static {
		if (!Skript.methodExists(Entity.class, "getPassengers")) {
			try {
				getPassenger = Entity.class.getDeclaredMethod("getPassenger");
				setPassenger = Entity.class.getDeclaredMethod("setPassenger", Entity.class);
			} catch (final NoSuchMethodException ex) {
				Skript.outdatedError(ex);
			} catch (final Exception ex) {
				Skript.exception(ex);
			} 
		}
	}

	public static Entity[] getPassenger(Entity e) {
		if (hasMultiplePassenger()) {
			return e.getPassengers().toArray(new Entity[0]);
		} else {
			try {
				return new Entity[]{(Entity)getPassenger.invoke(e)};		
			} catch (final Exception ex) { //I don't think it can happen, but just in case.
				Skript.exception(ex, "A error occured while trying to get a passenger in version lower than 1.11.2.");
			} 
		}
		return null;
	}
	/**
	 * Add the passenger to the vehicle
	 * @param vehicle - The entity vehicle
	 * @param passenger - The entity passenger
	 */
	public static void addPassenger(Entity vehicle, Entity passenger) {
		if (vehicle == null || passenger == null)
			return;
		if (hasMultiplePassenger()) {
			vehicle.addPassenger(passenger);
		} else {
			try {
				vehicle.eject();
				setPassenger.invoke(vehicle, passenger);
			} catch (final Exception ex) { 
				Skript.exception(ex, "A error occured while trying to set a passenger in version lower than 1.11.2.");
			}
		}
	}
	/**
	 * Remove the passenger from the vehicle.
	 * @param vehicle - The entity vehicle
	 * @param passenger - The entity passenger
	 */
	public static void removePassenger(Entity vehicle, Entity passenger){
		if (vehicle == null || passenger == null)
			return;
		if (hasMultiplePassenger()){
			vehicle.removePassenger(passenger);
		} else {
			vehicle.eject();
		}
	}
	/**
	 * @return True if it supports multiple passengers
	 */
	public static boolean hasMultiplePassenger(){
		return setPassenger == null;
	}
	
}
