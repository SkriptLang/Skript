package org.skriptlang.skript.bukkit.inventory.elements.events;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.*;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EvtInventory extends SkriptEvent {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtInventory.class, "Inventory Open")
				.addEvent(InventoryOpenEvent.class)
				.addPatterns("inventory open[ed]")
				.addDescription("Called when an inventory is opened for player.")
				.addExample(
					"""
						on inventory open:
							close player's inventory
						""")
				.addSince("2.2-dev21")
				.supplier(EvtInventory::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtInventory.class, "Inventory Close")
				.addEvent(InventoryCloseEvent.class)
				.addPatterns("inventory clos(ing|e[d])")
				.addDescription("Called when player's currently viewed inventory is closed.")
				.addExample(
					"""
						on inventory close:
							if player's location is {location}:
								send "You exited the shop!"
						""")
				.addSince("2.2-dev21")
				.supplier(EvtInventory::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtInventory.class, "Inventory Pickup")
				.addEvent(InventoryPickupItemEvent.class)
				.addPatterns("inventory pick[ ]up")
				.addDescription("Called when an inventory (a hopper, a hopper minecart, etc.) picks up an item")
				.addExample(
					"""
						on inventory pickup:
						""")
				.addSince("2.5.1")
				.supplier(EvtInventory::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtInventory.class, "Anvil Prepare")
				.addEvent(PrepareAnvilEvent.class)
				.addPatterns("anvil prepar(e|ing)")
				.addDescription("Called when an item is put in a slot for repair by an anvil. Please note that this event is called multiple times in a single item slot move.")
				.addExample(
					"""
						on anvil prepare:
							event-item is set # result item
							chance of 5%:
								set repair cost to repair cost * 50%
								send "You're LUCKY! You got 50% discount." to player
						""")
				.addSince("2.7")
				.supplier(EvtInventory::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtInventory.class, "Inventory Drag")
				.addEvent(InventoryDragEvent.class)
				.addPatterns("inventory drag[ging]")
				.addDescription("Called when a player drags an item in their cursor across the inventory.")
				.addExample(
					"""
						on inventory drag:
							if player's current inventory is {_gui}:
								send "You can't drag your items here!" to player
								cancel event
						""")
				.addSince("2.7")
				.supplier(EvtInventory::new)
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
		return "inventory event";
	}

}
