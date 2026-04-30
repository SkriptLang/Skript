package org.skriptlang.skript.bukkit.vehicle.elements.events;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.bukkit.event.Event;
import org.bukkit.event.vehicle.*;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EvtVehicle extends SkriptEvent {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtVehicle.class, "Vehicle Create")
				.addEvent(VehicleCreateEvent.class)
				.addPatterns("vehicle create", "creat(e|ing|ion of) [a] vehicle")
				.addDescription("Called when a new vehicle is created, e.g. when a player places a boat or minecart.")
				.addExample("on vehicle create:")
				.addSince("1.0")
				.supplier(EvtVehicle::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtVehicle.class, "Vehicle Damage")
				.addEvent(VehicleDamageEvent.class)
				.addPatterns("vehicle damage", "damag(e|ing) [a] vehicle")
				.addDescription("Called when a vehicle gets damage. Too much damage will destroy the vehicle.")
				.addExample("on vehicle damage:")
				.addSince("1.0")
				.supplier(EvtVehicle::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtVehicle.class, "Vehicle Destroy")
				.addEvent(VehicleDestroyEvent.class)
				.addPatterns("vehicle destroy", "destr(oy[ing]|uction of) [a] vehicle")
				.addDescription("Called when a vehicle is destroyed. Any passenger will be ejected and the vehicle might drop some item(s).")
				.addExample("""
					on vehicle destroy:
						cancel event
					""")
				.addSince("1.0")
				.supplier(EvtVehicle::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtVehicle.class, "Vehicle Enter")
				.addEvent(VehicleEnterEvent.class)
				.addPatterns("vehicle enter", "enter[ing] [a] vehicle")
				.addDescription("Called when an entity enters a vehicle, either deliberately (players) or by falling into them (mobs).")
				.addExample("""
					on vehicle enter:
						entity is a player
						cancel event
					""")
				.addSince("1.0")
				.supplier(EvtVehicle::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtVehicle.class, "Vehicle Exit")
				.addEvent(VehicleExitEvent.class)
				.addPatterns("vehicle exit", "exit[ing] [a] vehicle")
				.addDescription("Called when an entity exits a vehicle.")
				.addExample("""
					on vehicle exit:
						if event-entity is a spider:
							kill event-entity
					""")
				.addSince("1.0")
				.supplier(EvtVehicle::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtVehicle.class, "Vehicle Move")
				.addEvent(VehicleMoveEvent.class)
				.addPatterns("vehicle move")
				.addDescription(
					"Called when a vehicle moves.",
					"Please note that using this event can cause lag if there are multiple vehicle entities, i.e. Horse, Pig, Boat, Minecart")
				.addExample("""
					on vehicle move:
						broadcast past event-location
						broadcast event-location
					""")
				.addSince("2.10")
				.supplier(EvtVehicle::new)
				.build()
		);
	}

	@Override
	public boolean init(Literal<?>[] exprs, int matchedPattern, ParseResult parseResult) {
		return true;
	}

	@Override
	public boolean check(Event event) {
		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "vehicle event";
	}

}
