package org.skriptlang.skript.bukkit.block.elements.events;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.data.DefaultComparators;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.util.BlockStateBlock;
import ch.njol.skript.util.Direction;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import org.bukkit.event.Event;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue.Time;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.lang.comparator.Relation;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EvtBlockPlace extends SkriptEvent {

	public static void register(SyntaxRegistry syntaxRegistry, EventValueRegistry eventValueRegistry) {
		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(EvtBlockPlace.class, "Block Place")
			.supplier(EvtBlockPlace::new)
			.addEvents(CollectionUtils.array(BlockPlaceEvent.class, PlayerBucketEmptyEvent.class, HangingPlaceEvent.class))
			.addPattern("[block] (plac(e|ing)|build[ing]) [[of] %-itemtypes/blockdatas%]")
			.addDescription("Called when a player places a block.")
			.addExample("""
				on place:
				    send "Nice %event-item stack% you got there!" to player
				""")
			.addExample("""
				on place of a furnace, crafting table or chest:
				    send "Basic utilities eh?"
				""")
			.addSince("1.0, 2.6 (BlockData support)")
			.build());

		eventValueRegistry.register(EventValue.builder(BlockPlaceEvent.class, Player.class)
			.getter(BlockPlaceEvent::getPlayer)
			.build());

		eventValueRegistry.register(EventValue.builder(BlockPlaceEvent.class, ItemStack.class)
			.getter(BlockPlaceEvent::getItemInHand)
			.time(Time.PAST)
			.build());

		eventValueRegistry.register(EventValue.builder(BlockPlaceEvent.class, ItemStack.class)
			.getter(BlockPlaceEvent::getItemInHand)
			.build());

		eventValueRegistry.register(EventValue.builder(BlockPlaceEvent.class, ItemStack.class)
			.getter(event -> {
				ItemStack item = event.getItemInHand().clone();
				if (event.getPlayer().getGameMode() != GameMode.CREATIVE)
					item.setAmount(item.getAmount() - 1);
				return item;
			})
			.time(Time.FUTURE)
			.build());

		eventValueRegistry.register(EventValue.builder(BlockPlaceEvent.class, Block.class)
			.getter(event -> new BlockStateBlock(event.getBlockReplacedState()))
			.build());

		eventValueRegistry.register(EventValue.builder(BlockPlaceEvent.class, Direction.class)
			.getter(event -> {
				BlockFace blockFace = event.getBlockPlaced().getFace(event.getBlockAgainst());
				if (blockFace == null)
					return Direction.ZERO;
				return new Direction(new double[]{blockFace.getModX(), blockFace.getModY(), blockFace.getModZ()});
			})
			.build());
	}

	private @Nullable Literal<Object> types;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		if (args[0] != null)
			types = (Literal<Object>) args[0];
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (types == null)
			return true;

		ItemType item = null;
		BlockData blockData = null;

		switch (event) {
			case BlockPlaceEvent blockEvent -> {
				item = new ItemType(blockEvent.getBlock());
				blockData = blockEvent.getBlock().getBlockData();
			}
			case PlayerBucketEmptyEvent bucketEvent -> item = bucketEvent.getItemStack() != null ? new ItemType(bucketEvent.getItemStack()) : new ItemType();
			case HangingPlaceEvent hangingEvent -> {
				EntityData<?> data = EntityData.fromEntity(hangingEvent.getEntity());
				return types.check(event, object -> object instanceof ItemType itemType && Relation.EQUAL.isImpliedBy(DefaultComparators.entityItemComparator.compare(data, itemType)));
			}
			default -> {}
		}

		ItemType finalItem = item;
		BlockData finalBlockData = blockData;
		return types.check(event, object -> {
			if (object instanceof ItemType itemType)
				return itemType.isSupertypeOf(finalItem);
			else if (object instanceof BlockData value)
				return finalBlockData != null && finalBlockData.matches(value);
			return false;
		});
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append("block place")
			.appendIf(types != null,"of", types)
			.toString();
	}

}
