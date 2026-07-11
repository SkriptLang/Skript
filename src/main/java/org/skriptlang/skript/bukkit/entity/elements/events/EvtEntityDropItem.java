package org.skriptlang.skript.bukkit.entity.elements.events;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.LiteralList;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EvtEntityDropItem extends SkriptEvent {

	public static void register(SyntaxRegistry syntaxRegistry, EventValueRegistry eventValueRegistry) {
		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(EvtEntityDropItem.class, "Entity Drop Item")
			.supplier(EvtEntityDropItem::new)
			.addEvents(CollectionUtils.array(PlayerDropItemEvent.class, EntityDropItemEvent.class))
			.addPatterns("[%-entitydatas%] drop[ping] [[of] %-itemtypes%]")
			.addKeyword("drop")
			.addDescription("""
				   Called when an entity (including players) drops an item.
				   e.g. when a chicken lays an egg.
				   """)
			.addExample("""
				on player dropping of tnt:
					send "Don't waste perfectly good explosives!" to player
				""")
			.addExample("""
				on piglin drop ender pearl:
					broadcast "Time to beat the dragon!"
				""")
			.addExample("""
				on drop:
					broadcast "%event-item stack% was just dropped at %location of event-item stack%!"
				""")
			.addSince("unknown (before 2.1), 2.7 (entity), INSERT VERSION (entity data)")
			.build());

		eventValueRegistry.register(EventValue.builder(PlayerDropItemEvent.class, Player.class)
			.getter(PlayerDropItemEvent::getPlayer)
			.build());

		eventValueRegistry.register(EventValue.builder(PlayerDropItemEvent.class, Item.class)
			.getter(PlayerDropItemEvent::getItemDrop)
			.build());

		eventValueRegistry.register(EventValue.builder(PlayerDropItemEvent.class, ItemStack.class)
			.getter(event -> event.getItemDrop().getItemStack())
			.build());

		eventValueRegistry.register(EventValue.builder(PlayerDropItemEvent.class, Entity.class)
			.getter(PlayerDropItemEvent::getPlayer)
			.build());

		eventValueRegistry.register(EventValue.builder(EntityDropItemEvent.class, Item.class)
			.getter(EntityDropItemEvent::getItemDrop)
			.build());

		eventValueRegistry.register(EventValue.builder(EntityDropItemEvent.class, ItemStack.class)
			.getter(event -> event.getItemDrop().getItemStack())
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
		boolean entityDataMatches = true;
		boolean itemTypeMatches = true;

		Entity entity;
		Item drop;

		if (event instanceof PlayerDropItemEvent playerEvent) {
			entity = playerEvent.getPlayer();
			drop = playerEvent.getItemDrop();
		} else if (event instanceof EntityDropItemEvent entityEvent ){
			entity = entityEvent.getEntity();
			drop = entityEvent.getItemDrop();
		} else {
			entity = null;
			drop = null;
		}

		if (entityData != null)
			entityDataMatches = entityData.check(event, data -> data.isInstance(entity));
		if (itemType != null) {
			if (drop == null)
				return false;
			itemTypeMatches = itemType.check(event, item -> item.isOfType(drop.getItemStack()));
		}

		return entityDataMatches && itemTypeMatches;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.appendIf(entityData != null, entityData)
			.append("dropping")
			.appendIf(itemType != null, "of", itemType)
			.toString();
	}

}
