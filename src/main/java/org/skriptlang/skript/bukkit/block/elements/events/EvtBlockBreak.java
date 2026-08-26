package org.skriptlang.skript.bukkit.block.elements.events;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.data.DefaultComparators;
import ch.njol.skript.entity.EntityData;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
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

public class EvtBlockBreak extends SkriptEvent {

	public static void register(SyntaxRegistry syntaxRegistry, EventValueRegistry eventValueRegistry) {
		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(EvtBlockBreak.class, "Block Break")
			.supplier(EvtBlockBreak::new)
			.addEvents(CollectionUtils.array(BlockBreakEvent.class, PlayerBucketFillEvent.class, HangingBreakEvent.class))
			.addPattern("[block] (break[ing]|1¦min(e|ing)) [[of] %-itemtypes/blockdatas%]")
			.addDescription("""
				Called when a block is broken by a player.
				Note that using 'on mine' will make it so the event is only called if the event-block has drops.
				""")
			.addExample("""
				on break of tnt:
					send "Nice job preventing disaster!" to player
				""")
			.addExample("""
				on mine of coal ore:
					send "You just got %sizes of drops of event-block% drops!" to player
				""")
			.addExample("""
				on break of chest[facing=north]:
					send "This chest seems to like facing north a lot.." to player
				""")
			.addExample("""
				on break of potatoes[age=7]:
					send "At least you harvested it during harvest season.." to player
					give player 15 potatoes
				""")
			.addSince("1.0 (break, mine)")
			.addSince("2.6 (BlockData support)")
			.build());

		eventValueRegistry.register(EventValue.builder(BlockBreakEvent.class, Player.class)
			.getter(BlockBreakEvent::getPlayer)
			.build());

		eventValueRegistry.register(EventValue.builder(BlockBreakEvent.class, Block.class)
			.getter(BlockEvent::getBlock)
			.time(Time.PAST)
			.build());
	}

	private @Nullable Literal<Object> types;

	private boolean mine = false;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		if (args[0] != null)
			types = (Literal<Object>) args[0];
		mine = parseResult.mark == 1;
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (event instanceof BlockBreakEvent blockEvent && mine)
			return !blockEvent.getBlock().getDrops(blockEvent.getPlayer().getInventory().getItemInMainHand()).isEmpty();

		if (types == null)
			return true;

		ItemType item = null;
		BlockData blockData = null;

		switch (event) {
			case BlockBreakEvent blockEvent -> {
				item = new ItemType(blockEvent.getBlock());
				blockData = blockEvent.getBlock().getBlockData();
			}
			case PlayerBucketFillEvent playerEvent -> {
				Block block = playerEvent.getBlockClicked();
				item = new ItemType(block);
				blockData = block.getBlockData();
			}
			case PlayerBucketEmptyEvent playerEvent -> {
				if (playerEvent.getItemStack() != null) {
					item = new ItemType(playerEvent.getItemStack());
				} else {
					item = new ItemType();
				}
			}
			case HangingEvent hangingEvent -> {
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
			else if (object instanceof BlockData bd)
				return finalBlockData != null && finalBlockData.matches(bd);
			return false;
		});
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append("block", mine ? "mine" : "break")
			.appendIf(types != null,"of", types)
			.toString();
	}

}
