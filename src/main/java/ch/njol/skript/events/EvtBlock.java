package ch.njol.skript.events;

import com.destroystokyo.paper.MaterialSetTag;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.Event;
import org.bukkit.event.block.*;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import org.skriptlang.skript.lang.comparator.Relation;
import ch.njol.skript.classes.data.DefaultComparators;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Classes;

import java.util.Arrays;

@SuppressWarnings("unchecked")
public class EvtBlock extends SkriptEvent {

	static {
		// REMIND attacking an item frame first removes its item; include this in on block damage?
		Skript.registerEvent("Break / Mine", EvtBlock.class, new Class[]{BlockBreakEvent.class, PlayerBucketFillEvent.class, HangingBreakEvent.class}, "[block] (break[ing]|1¦min(e|ing)) [[of] %-itemtypes/blockdatas%]")
				.description("Called when a block is broken by a player. If you use 'on mine', only events where the broken block dropped something will call the trigger.")
				.examples("on mine:", "on break of stone:", "on break of chest[facing=north]:", "on break of potatoes[age=7]:")
				.since("1.0 (break), unknown (mine), 2.6 (BlockData support)");
		Skript.registerEvent("Burn", EvtBlock.class, BlockBurnEvent.class, "[block] burn[ing] [[of] %-itemtypes/blockdatas%]")
				.description("Called when a block is destroyed by fire.")
				.examples("on burn:", "on burn of oak wood, oak fences, or chests:", "on burn of oak_log[axis=y]:")
				.since("1.0, 2.6 (BlockData support)");
		Skript.registerEvent("Place", EvtBlock.class, new Class[]{BlockPlaceEvent.class, PlayerBucketEmptyEvent.class, HangingPlaceEvent.class}, "[block] (plac(e|ing)|build[ing]) [[of] %-itemtypes/blockdatas%]")
				.description("Called when a player places a block.")
				.examples("on place:", "on place of a furnace, crafting table or chest:", "on break of chest[type=right] or chest[type=left]")
				.since("1.0, 2.6 (BlockData support)");
		Skript.registerEvent("Fade", EvtBlock.class, BlockFadeEvent.class, "[block] fad(e|ing) [[of] %-itemtypes/blockdatas%]")
				.description("Called when a block 'fades away', e.g. ice or snow melts.")
				.examples("on fade of snow or blue ice:", "on fade of snow[layers=2]")
				.since("1.0, 2.6 (BlockData support)");
		Skript.registerEvent("Form", EvtBlock.class, BlockFormEvent.class, "[block] form[ing] [[of] %-itemtypes/blockdatas%]")
				.description("Called when a block is created, but not by a player, e.g. snow forms due to snowfall, water freezes in cold biomes. This isn't called when block spreads (mushroom growth, water physics etc.), as it has its own event (see <a href='#spread'>spread event</a>).")
				.examples("on form of snow:")
				.since("1.0, 2.6 (BlockData support)");
		Skript.registerEvent("Block Drop", EvtBlock.class, BlockDropItemEvent.class, "block drop[ping] [[of] %-itemtypes/blockdatas%]")
				.description(
					"Called when a block broken by a player drops something.",
					"<ul>",
					"<li>event-player: The player that broke the block</li>",
					"<li>past event-block: The block that was broken</li>",
					"<li>event-block: The block after being broken</li>",
					"<li>event-items (or drops): The drops of the block</li>",
					"<li>event-entities: The entities of the dropped items</li>",
					"</ul>",
					"",
					"If the breaking of the block leads to others being broken, such as torches, they will appear" +
					"in \"event-items\" and \"event-entities\"."
				)
				.examples(
					"on block drop:",
						"\tbroadcast event-player",
						"\tbroadcast past event-block",
						"\tbroadcast event-block",
						"\tbroadcast event-items",
						"\tbroadcast event-entities",
					"on block drop of oak log:"
				)
				.since("2.10");
		Skript.registerEvent("Block Damage", EvtBlock.class, BlockDamageEvent.class, "block damag(ing|e) [[of] %-itemtypes/blockdatas%]")
				.description("Called when a player starts to break a block. You can usually just use the leftclick event for this.")
				.examples(
					"on block damage:",
						"\tif block is tagged with minecraft tag \"logs\":",
							"\t\tsend \"You can't break the holy log!\"",
					"on block damaging of stone:",
						"\tbroadcast block hardness of event-block"
				)
				.since("1.0, INSERT VERSION (type expression)");
		Skript.registerEvent("Block Trample", EvtBlock.class, PlayerInteractEvent.class, "(crop:crop|block) trampl(ing|e) [[of] %-itemtypes/blockdatas%]")
				.description("Called when a player tramples on a block like wheat breaking or redstone ore from jumping on it.")
				.examples("on block trampling of wheat:", "\tcancel event:")
				.since("INSERT VERSION");
	}

	private @Nullable Literal<Object> types;
	private boolean mine = false;
	private boolean crop = false;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		types = (Literal<Object>) args[0];
		mine = parseResult.mark == 1;
		if (types != null && parseResult.hasTag("crops")) {
			var tag = MaterialSetTag.CROPS;
			crop = Arrays.stream(types.getAll())
				.filter(ItemType.class::isInstance)
				.anyMatch(itemType -> tag.isTagged(((ItemType) itemType).getMaterial()));
			if (!crop) {
				Skript.error("The crop trample event only accepts types of 'crop' material tag, but found " + Classes.toString(types));
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (mine && event instanceof BlockBreakEvent blockBreakEvent) {
			if (blockBreakEvent.getBlock().getDrops(blockBreakEvent.getPlayer().getInventory().getItemInMainHand()).isEmpty())
				return false;
		}
		if (types == null && !(event instanceof PlayerInteractEvent))
			return true;

		ItemType item;
		BlockData blockData = null;

		switch (event) {
			case PlayerInteractEvent playerInteractEvent -> {
				var block = playerInteractEvent.getClickedBlock();
				if (playerInteractEvent.getAction() != Action.PHYSICAL || block == null || crop && block.getType() != Material.FARMLAND)
					return false;

				BlockState newState = block.getRelative(BlockFace.UP).getState();
				if (crop && !MaterialSetTag.CROPS.isTagged(newState.getType()))
					return false;

				if (types == null)
					return true;

				item = new ItemType(newState.getBlockData());
				blockData = newState.getBlockData();
			}
			case BlockFormEvent blockFormEvent -> {
				BlockState newState = blockFormEvent.getNewState();
				item = new ItemType(newState.getBlockData());
				blockData = newState.getBlockData();
			}
			case BlockDropItemEvent blockDropItemEvent -> {
				Block block = blockDropItemEvent.getBlock();
				item = new ItemType(block);
				blockData = block.getBlockData();
			}
			case BlockDamageEvent blockDamageEvent -> {
				BlockState newState = blockDamageEvent.getBlock().getState();
				item = new ItemType(newState.getBlockData());
				blockData = newState.getBlockData();
			}
			case BlockEvent blockEvent -> {
				Block block = blockEvent.getBlock();
				item = new ItemType(block);
				blockData = block.getBlockData();
			}
			case PlayerBucketFillEvent playerBucketFillEvent -> {
				Block block = playerBucketFillEvent.getBlockClicked();
				item = new ItemType(block);
				blockData = block.getBlockData();
			}
			case PlayerBucketEmptyEvent playerBucketEmptyEvent -> {
				var itemStack = playerBucketEmptyEvent.getItemStack();
				if (itemStack == null)
					return false;

				item = new ItemType(playerBucketEmptyEvent.getItemStack());
			}
			case HangingEvent hangingEvent -> {
				EntityData<?> d = EntityData.fromEntity((hangingEvent.getEntity()));
				return types.check(event, o ->
					o instanceof ItemType && Relation.EQUAL.isImpliedBy(DefaultComparators.entityItemComparator.compare(d, ((ItemType) o)))
				);
			}
			case null, default -> {
				assert false;
				return false;
			}
		}

		final ItemType itemF = item;
		BlockData finalBlockData = blockData;

		return types.check(event, o -> {
			if (o instanceof ItemType)
				return ((ItemType) o).isSupertypeOf(itemF);
			else if (o instanceof BlockData && finalBlockData != null)
				return finalBlockData.matches(((BlockData) o));
			return false;
		});
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "break/place/burn/fade/form/drop/block damage of " + Classes.toString(types);
	}

}
