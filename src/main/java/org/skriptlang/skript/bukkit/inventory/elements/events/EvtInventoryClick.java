package org.skriptlang.skript.bukkit.inventory.elements.events;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.lang.LiteralList;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.util.slot.EquipmentSlot;
import ch.njol.skript.util.slot.InventorySlot;
import ch.njol.skript.util.slot.Slot;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.Nullable;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import org.bukkit.event.Event;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EvtInventoryClick extends SkriptEvent {

	public static void register(SyntaxRegistry syntaxRegistry, EventValueRegistry eventValueRegistry) {
		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(EvtInventoryClick.class, "Inventory Click")
			.supplier(EvtInventoryClick::new)
			.addEvent(InventoryClickEvent.class)
			.addPattern("[player] inventory(-| )click[ing] [[at|on] %-itemtypes%]")
			.addDescription("Called when a player clicks on an inventory slot.")
			.addExample("""
				on inventory click:
					if event-item is tnt:
						broadcast "Explosives found!"
						kill player
				""")
			.addExample("""
				on inventory click at bedrock:
					cancel event
					send "You cannot click on an illegal item!"
				""")
			.addSince("2.2-Fixes-V10, INSERT VERSION (on)")
			.build());

		eventValueRegistry.register(EventValue.builder(InventoryClickEvent.class, ItemStack.class)
			.getter(InventoryClickEvent::getCurrentItem)
			.build());

		eventValueRegistry.register(EventValue.builder(InventoryClickEvent.class, Slot.class)
			.getter(event -> {
				Inventory inventory = event.getClickedInventory();

				if (inventory == null)
					return null;
				int slotIndex = event.getSlot();

				// Not all indices point to inventory slots. Equipment, for example
				if (inventory instanceof PlayerInventory playerInventory && slotIndex >= 36) {
					return new EquipmentSlot(playerInventory.getHolder(), slotIndex);
				} else {
					return new InventorySlot(inventory, slotIndex, event.getRawSlot());
				}
			})
			.build());

		eventValueRegistry.register(EventValue.builder(InventoryClickEvent.class, InventoryAction.class)
			.getter(InventoryClickEvent::getAction)
			.build());

		eventValueRegistry.register(EventValue.builder(InventoryClickEvent.class, ClickType.class)
			.getter(InventoryClickEvent::getClick)
			.build());

		eventValueRegistry.register(EventValue.builder(InventoryClickEvent.class, Inventory.class)
			.getter(InventoryClickEvent::getClickedInventory)
			.build());

	}

	private @Nullable Literal<ItemType> itemType;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		if (args[0] != null) {
			itemType = (Literal<ItemType>) args[0];
			if (itemType.getAnd() && itemType instanceof LiteralList<ItemType> list)
				list.invertAnd();
		}
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (itemType == null)
			return true;

		InventoryClickEvent inventoryEvent = (InventoryClickEvent) event;

		return itemType.check(event, itemType -> itemType.isOfType(inventoryEvent.getCurrentItem()));
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append("player inventory click")
			.appendIf(itemType != null, "on", itemType)
			.toString();
	}

}
