package org.skriptlang.skript.bukkit.inventory.elements;

import ch.njol.skript.bukkitutil.InventoryUtils;
import ch.njol.skript.lang.util.SimpleEvent;
import ch.njol.skript.util.slot.InventorySlot;
import ch.njol.skript.util.slot.Slot;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.event.inventory.InventoryCloseEvent.Reason;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue.Time;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InventoryEvents {

	public static void register(SyntaxRegistry syntaxRegistry, EventValueRegistry eventValueRegistry) {

		// Inventory Event Values

		eventValueRegistry.register(EventValue.builder(InventoryEvent.class, Inventory.class)
			.getter(InventoryEvent::getInventory)
			.build());

		eventValueRegistry.register(EventValue.builder(InventoryInteractEvent.class, Player.class)
			.getter(event -> event.getWhoClicked() instanceof Player player ? player : null)
			.build());

		eventValueRegistry.register(EventValue.builder(InventoryInteractEvent.class, World.class)
			.getter(event -> event.getWhoClicked().getWorld())
			.build());

		// Inventory Events

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Player Inventory Open")
			.addEvent(InventoryOpenEvent.class)
			.addPatterns("[player] inventory open[ed]", "[player] open[ing] [an] inventory")
			.addDescription("""
				Called when an inventory is opened for a player.
				Note that this event is not called when a player opens their own inventory.
				""")
			.addExample("""
				on inventory open:
					send "its an inventory!" to player
				""")
			.addSince("2.2-dev21, INSERT VERSION (added new pattern and updated existing one)")
			.supplier(() -> new SimpleEvent("player inventory open"))
			.build());

		eventValueRegistry.register(EventValue.builder(InventoryOpenEvent.class, Player.class)
			.getter(event -> (Player) event.getPlayer())
			.build());

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Player Inventory Close")
			.addEvent(InventoryCloseEvent.class)
			.addPatterns("[player] inventory clos(ing|e[d])", "[player] clos[e|ing] inventory")
			.addDescription("""
				Called when a player closes the inventory they are currently viewing.
				Note that this event is not called when a player closes their own inventory.
				""")
			.addExample("""
				on player close inventory:
					send "closed!" to player
				""")
			.addSince("2.2-dev21, INSERT VERSION (updated pattern)")
			.supplier(() -> new SimpleEvent("player inventory close"))
			.build());

		eventValueRegistry.register(EventValue.builder(InventoryCloseEvent.class, Player.class)
			.getter(event -> (Player) event.getPlayer())
			.build());

		eventValueRegistry.register(EventValue.builder(InventoryCloseEvent.class, Reason.class)
			.getter(InventoryCloseEvent::getReason)
			.build());

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Inventory Pickup Item")
			.addEvent(InventoryPickupItemEvent.class)
			.addPatterns("inventory pick[ ]up [item]")
			.addDescription("Called when an inventory (a hopper, a hopper minecart, etc.) picks up an item.")
			.addExample("""
				on inventory pickup:
					broadcast "An inventory just picked up an item!"
				""")
			.addSince("2.5.1, INSERT VERSION (updated pattern)")
			.supplier(() -> new SimpleEvent("inventory pick up"))
			.build());

		// not an inventory event for some reason??
		eventValueRegistry.register(EventValue.builder(InventoryPickupItemEvent.class, Inventory.class)
			.getter(InventoryPickupItemEvent::getInventory)
			.build());

		eventValueRegistry.register(EventValue.builder(InventoryPickupItemEvent.class, Item.class)
			.getter(InventoryPickupItemEvent::getItem)
			.build());

		eventValueRegistry.register(EventValue.builder(InventoryPickupItemEvent.class, ItemStack.class)
			.getter(event -> event.getItem().getItemStack())
			.build());

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Inventory Drag Item")
			.addEvent(InventoryDragEvent.class)
			.addPatterns("inventory drag[ging] [item]")
			.addDescription("Called when a player drags an item in their cursor across the inventory.")
			.addExample("""
				on inventory drag item:
					if player's top inventory is {example}:
						sends "You cannot drag items here!" to player
						cancel event
				""")
			.addSince("2.7, INSERT VERSION (updated pattern)")
			.supplier(() -> new SimpleEvent("inventory drag item"))
			.build());

		eventValueRegistry.register(EventValue.builder(InventoryDragEvent.class, Player.class)
			.getter(event -> event.getWhoClicked() instanceof Player player ? player : null)
			.build());

		eventValueRegistry.register(EventValue.builder(InventoryDragEvent.class, World.class)
			.getter(event -> event.getWhoClicked().getWorld())
			.build());

		eventValueRegistry.register(EventValue.builder(InventoryDragEvent.class, ItemStack.class)
			.getter(InventoryDragEvent::getOldCursor)
			.time(Time.PAST)
			.build());

		eventValueRegistry.register(EventValue.builder(InventoryDragEvent.class, ItemStack.class)
			.getter(InventoryDragEvent::getCursor)
			.build());

		eventValueRegistry.register(EventValue.builder(InventoryDragEvent.class, ItemStack[].class)
			.getter(event -> event.getNewItems().values().toArray(new ItemStack[0]))
			.build());

		eventValueRegistry.register(EventValue.builder(InventoryDragEvent.class, Slot[].class)
			.getter(event -> {
				List<Slot> slots = new ArrayList<>(event.getRawSlots().size());
				InventoryView view = event.getView();
				for (Integer rawSlot : event.getRawSlots()) {
					Inventory inventory = InventoryUtils.getInventory(view, rawSlot);
					Integer slot = InventoryUtils.convertSlot(view, rawSlot);
					if (inventory == null || slot == null)
						continue;
					// Not all indices point to inventory slots. Equipment, for example
					if (inventory instanceof PlayerInventory && slot >= 36) {
						slots.add(new ch.njol.skript.util.slot.EquipmentSlot(((PlayerInventory) view.getBottomInventory()).getHolder(), slot));
					} else {
						slots.add(new InventorySlot(inventory, slot));
					}
				}
				return slots.toArray(new Slot[0]);
			})
			.build());

		eventValueRegistry.register(EventValue.builder(InventoryDragEvent.class, ClickType.class)
			.getter(event -> event.getType() == DragType.EVEN ? ClickType.LEFT : ClickType.RIGHT)
			.build());

		eventValueRegistry.register(EventValue.builder(InventoryDragEvent.class, Inventory[].class)
			.getter(event -> {
				Set<Inventory> inventories = new HashSet<>();
				InventoryView view = event.getView();
				for (Integer rawSlot : event.getRawSlots()) {
					Inventory inventory = InventoryUtils.getInventory(view, rawSlot);
					if (inventory != null)
						inventories.add(inventory);
				}
				return inventories.toArray(new Inventory[0]);
			})
			.build());

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Inventory Item Move")
			.addEvent(InventoryMoveItemEvent.class)
			.addPatterns(
				"inventory item (move|transport)",
				"inventory (mov(e|ing)|transport[ing]) [an] item"
			)
			.addDescription("""
				Called when an entity or block (e.g. hopper) tries to move items directly from one inventory to another.
				Note that when this event is called, the initiator may have already removed the item from the source inventory and is ready to move it into the destination inventory.
				If this event is cancelled, the items will be returned to the source inventory.
				""")
			.addExample("""
				on inventory item move:
					broadcast "%holder of past event-inventory% is transporting %event-item% to %holder of event-inventory%!"
				""")
			.addSince("2.8.0")
			.supplier(() -> new SimpleEvent("inventory item move"))
			.build());

		eventValueRegistry.register(EventValue.builder(InventoryMoveItemEvent.class, Inventory.class)
			.getter(InventoryMoveItemEvent::getSource)
			.build());

		eventValueRegistry.register(EventValue.builder(InventoryMoveItemEvent.class, Inventory.class)
			.getter(InventoryMoveItemEvent::getDestination)
			.time(Time.FUTURE)
			.build());

		eventValueRegistry.register(EventValue.builder(InventoryMoveItemEvent.class, Block.class)
			.getter(event -> {
				Location location = event.getDestination().getLocation();
				if (location == null)
					return null;

				return location.getBlock();
			})
			.build());

		eventValueRegistry.register(EventValue.builder(InventoryMoveItemEvent.class, Block.class)
			.getter(event -> {
				Location location = event.getSource().getLocation();
				if (location == null)
					return null;

				return location.getBlock();
			})
			.time(Time.FUTURE)
			.build());

		eventValueRegistry.register(EventValue.builder(InventoryMoveItemEvent.class, ItemStack.class)
			.getter(InventoryMoveItemEvent::getItem)
			.build());
	}

}
