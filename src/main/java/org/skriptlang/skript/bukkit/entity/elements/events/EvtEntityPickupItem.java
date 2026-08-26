package org.skriptlang.skript.bukkit.entity.elements.events;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.LiteralList;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import org.bukkit.entity.Item;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EvtEntityPickupItem extends SkriptEvent {

	public static void register(SyntaxRegistry syntaxRegistry, EventValueRegistry eventValueRegistry) {
		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(EvtEntityPickupItem.class, "Entity Pickup Item")
			.supplier(EvtEntityPickupItem::new)
			.addEvent(EntityPickupItemEvent.class)
			.addPatterns("[%-entitydatas%] (pick[ ]up|picking up) [[of] %-itemtypes%]")
			.addKeyword("pickup")
			.addDescription("""
				   Called when an entity (including players) picks up an item.
				   Note that the item is still on the ground when this event is called.
				   """)
			.addExample("""
				on player pickup:
					broadcast "Great just what I need: More junk.."
				""")
			.addExample("""
				on entity pickup of iron chestplate:
					cancel event
					broadcast "%event-entity% tried to equip themself with gear, but failed!"
				""")
			.addExample("""
				on player pickup of bedrock:
					broadcast "%player% just got a forbidden item! Get them!!"
				""")
			.addSince("unknown (before 2.1)")
			.addSince("2.5 (entity)")
			.addSince("INSERT VERSION (entity data)")
			.build());

		eventValueRegistry.register(EventValue.builder(EntityPickupItemEvent.class, Item.class)
			.getter(EntityPickupItemEvent::getItem)
			.build());

		eventValueRegistry.register(EventValue.builder(EntityPickupItemEvent.class, ItemStack.class)
			.getter(event -> event.getItem().getItemStack())
			.build());
	}

	private @Nullable Literal<EntityData<?>> entityData;
	private @Nullable Literal<ItemType> itemType;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		if (args[0] != null) {
			entityData = (Literal<EntityData<?>>) args[0];
			if (entityData.getAnd() && entityData instanceof LiteralList<EntityData<?>> list)
				list.invertAnd();
		}
		if (args[1] != null) {
			itemType = (Literal<ItemType>) args[1];
			if (itemType.getAnd() && itemType instanceof LiteralList<ItemType> list)
				list.invertAnd();
		}
		return true;
	}

	@Override
	public boolean check(Event event) {
		EntityPickupItemEvent entityEvent = (EntityPickupItemEvent) event;

		boolean entityDataMatches = true;
		boolean itemTypeMatches = true;

		if (entityData != null)
			entityDataMatches = entityData.check(event, data -> data.isInstance(entityEvent.getEntity()));

		if (itemType != null)
			itemTypeMatches = itemType.check(event, item -> item.isOfType(entityEvent.getItem().getItemStack()));

		return entityDataMatches && itemTypeMatches;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.appendIf(entityData != null, entityData)
			.append("picking up")
			.appendIf(itemType != null, "of", itemType)
			.toString();
	}

}
