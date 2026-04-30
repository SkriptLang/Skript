package org.skriptlang.skript.bukkit.block.elements.events;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.data.DefaultComparators;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.coll.CollectionUtils;
import com.destroystokyo.paper.event.block.AnvilDamagedEvent;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.Event;
import org.bukkit.event.block.*;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.lang.comparator.Relation;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EvtBlock extends SkriptEvent {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtBlock.class, "Can Build Check")
				.addEvent(BlockCanBuildEvent.class)
				.addPatterns("[block] can build check")
				.addDescription("Called when a player rightclicks on a block while holding a block or a placeable item. You can either cancel the event to prevent the block from being built, or uncancel it to allow it.",
					"Please note that the <a href='#ExprDurability'>data value</a> of the block to be placed is not available in this event, only its <a href='#ExprIdOf'>ID</a>.")
				.addExample(
					"""
						on block can build check:
							cancel event
						""")
				.addSince("1.0 (basic), 2.0 ([un]cancellable)")
				.supplier(EvtBlock::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtBlock.class, "Block Damage")
				.addEvent(BlockDamageEvent.class)
				.addPatterns("block damag(ing|e)")
				.addDescription("Called when a player starts to break a block. You can usually just use the leftclick event for this.")
				.addExample(
					"""
						on block damaging:
							if block is tagged with minecraft tag "logs":
								send "You can't break the holy log!"
						""")
				.addSince("1.0")
				.supplier(EvtBlock::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtBlock.class, "Flow")
				.addEvent(BlockFromToEvent.class)
				.addPatterns("[block] flow[ing]", "block mov(e|ing)")
				.addDescription("Called when a blocks flows or teleports to another block. This not only applies to water and lava, but teleporting dragon eggs as well.")
				.addExample(
					"""
						on block flow:
							if event-block is water:
								broadcast "Build more dams! It's starting to get wet in here"
						""")
				.addSince("1.0")
				.supplier(EvtBlock::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtBlock.class, "Ignition")
				.addEvent(BlockIgniteEvent.class)
				.addPatterns("[block] ignit(e|ion)")
				.addDescription("Called when a block starts burning, i.e. a fire block is placed next to it and this block is flammable.",
					"The <a href='#burn'>burn event</a> will be called when the block is about do be destroyed by the fire.")
				.addExample(
					"""
						on block ignite:
							if event-block is a ladder:
								cancel event
						""")
				.addSince("1.0")
				.supplier(EvtBlock::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtBlock.class, "Physics")
				.addEvent(BlockPhysicsEvent.class)
				.addPatterns("[block] physics")
				.addDescription("Called when a physics check is done on a block. By cancelling this event you can prevent some things from happening, " +
					"e.g. sand falling, dirt turning into grass, torches dropping if their supporting block is destroyed, etc." +
					"Please note that using this event might cause quite some lag since it gets called extremely often.")
				.addExample(
					"""
						# prevents sand from falling
						on block physics:
							block is sand
							cancel event
						""")
				.addSince("1.4.6")
				.supplier(EvtBlock::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtBlock.class, "Piston Extend")
				.addEvent(BlockPistonExtendEvent.class)
				.addPatterns("piston extend[ing]")
				.addDescription("Called when a piston is about to extend.")
				.addExample(
					"""
						on piston extend:
							broadcast "A piston is extending!"
						""")
				.addSince("1.0")
				.supplier(EvtBlock::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtBlock.class, "Piston Retract")
				.addEvent(BlockPistonRetractEvent.class)
				.addPatterns("piston retract[ing]")
				.addDescription("Called when a piston is about to retract.")
				.addExample(
					"""
						on piston retract:
							broadcast "A piston is retracting!"
						""")
				.addSince("1.0")
				.supplier(EvtBlock::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtBlock.class, "Redstone")
				.addEvent(BlockRedstoneEvent.class)
				.addPatterns("redstone [current] [chang(e|ing)]")
				.addDescription("Called when the redstone current of a block changes. This event is of not much use yet.")
				.addExample(
					"""
						on redstone change:
							send "someone is using redstone" to console
						""")
				.addSince("1.0")
				.supplier(EvtBlock::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtBlock.class, "Spread")
				.addEvent(BlockSpreadEvent.class)
				.addPatterns("spread[ing]")
				.addDescription("Called when a new block <a href='#form'>forms</a> as a result of a block that can spread, e.g. water or mushrooms.")
				.addExample("on spread:")
				.addSince("1.0")
				.supplier(EvtBlock::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtBlock.class, "Sign Change")
				.addEvent(SignChangeEvent.class)
				.addPatterns("sign (chang[e]|edit)[ing]", "[player] (chang[e]|edit)[ing] [a] sign")
				.addDescription("As signs are placed empty, this event is called when a player is done editing a sign.")
				.addExample(
					"""
						on sign change:
							line 2 is empty
							set line 1 to "<red>%line 1%"
						""")
				.addSince("1.0")
				.supplier(EvtBlock::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtBlock.class, "Block Fertilize")
				.addEvent(BlockFertilizeEvent.class)
				.addPatterns("[block] fertilize")
				.addDescription("Called when a player fertilizes blocks.")
				.addRequiredPlugins("Minecraft 1.13 or newer")
				.addExample(
					"""
						on block fertilize:
							send "Fertilized %size of fertilized blocks% blocks got fertilized."
						""")
				.addSince("2.5")
				.supplier(EvtBlock::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtBlock.class, "Leaves Decay")
				.addEvent(LeavesDecayEvent.class)
				.addPatterns("leaves decay[ing]")
				.addDescription("Called when a leaf block decays due to not being connected to a tree.")
				.addSince("1.0")
				.supplier(EvtBlock::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtBlock.class, "Sponge Absorb")
				.addEvent(SpongeAbsorbEvent.class)
				.addPatterns("sponge absorb")
				.addDescription("Called when a sponge absorbs blocks.")
				.addRequiredPlugins("Minecraft 1.13 or newer")
				.addExample(
					"""
						on sponge absorb:
							loop absorbed blocks:
								broadcast "%loop-block% was absorbed by a sponge!"
						""")
				.addSince("2.5")
				.supplier(EvtBlock::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtBlock.class, "Bell Ring")
				.addEvent(BellRingEvent.class)
				.addPatterns("bell ring[ing]")
				.addDescription("Called when a bell is rung.")
				.addExample(
					"""
						on bell ring:
							send "<gold>Ding-dong!<reset>" to all players in radius 10 of event-block
						""")
				.addSince("2.9.0")
				.supplier(EvtBlock::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtBlock.class, "Bell Resonate")
				.addEvent(BellResonateEvent.class)
				.addPatterns("bell resonat(e|ing)")
				.addDescription("Called when a bell resonates, highlighting nearby raiders.")
				.addExample(
					"""
						on bell resonate:
							send "<red>Raiders are nearby!" to all players in radius 32 around event-block
						""")
				.addSince("2.9.0")
				.supplier(EvtBlock::new)
				.build()
		);

		if (Skript.classExists("org.bukkit.event.block.VaultDisplayItemEvent")) {
			registry.register(
				BukkitSyntaxInfos.Event.KEY,
				BukkitSyntaxInfos.Event.builder(EvtBlock.class, "Vault Display Item")
					.addEvent(VaultDisplayItemEvent.class)
					.addPatterns("vault display[ing] item")
					.addDescription("Called when a vault in a trial chamber is about to display an item.")
					.addRequiredPlugins("Minecraft 1.21.1+")
					.addExample(
						"""
							on vault display item:
								set event-item to a netherite ingot
							""")
					.addSince("2.12")
					.supplier(EvtBlock::new)
					.build()
			);
		}

		if (Skript.classExists("com.destroystokyo.paper.event.block.AnvilDamagedEvent")) {
			registry.register(
				BukkitSyntaxInfos.Event.KEY,
				BukkitSyntaxInfos.Event.builder(EvtBlock.class, "Anvil Damage")
					.addEvent(AnvilDamagedEvent.class)
					.addPatterns("anvil damag(e|ing)")
					.addDescription("Called when an anvil is damaged/broken from being used to repair/rename items.",
						"Note: this does not include anvil damage from falling.")
					.addExample(
						"""
							on anvil damage:
								cancel event
							""")
					.addSince("2.7")
					.supplier(EvtBlock::new)
					.build()
			);
		}

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtBlock.class, "Block Break / Mine")
				.addEvents(CollectionUtils.array(BlockBreakEvent.class, PlayerBucketFillEvent.class, HangingBreakEvent.class))
				.addPatterns("[block] (break[ing]|1¦min(e|ing)) [[of] %-itemtypes/blockdatas%]")
				.addDescription("Called when a block is broken by a player. If you use 'on mine', only events where the broken block dropped something will call the trigger.")
				.addExample("""
					on mine:
					on break of stone:
					on break of chest[facing=north]:
					on break of potatoes[age=7]:
					""")
				.addSince("1.0 (break), unknown (mine), 2.6 (BlockData support)")
				.supplier(EvtBlock::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtBlock.class, "Block Burn")
				.addEvent(BlockBurnEvent.class)
				.addPatterns("[block] burn[ing] [[of] %-itemtypes/blockdatas%]")
				.addDescription("Called when a block is destroyed by fire.")
				.addExample("""
					on burn:
					on burn of oak wood, oak fences, or chests:
					on burn of oak_log[axis=y]:
					""")
				.addSince("1.0, 2.6 (BlockData support)")
				.supplier(EvtBlock::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtBlock.class, "Block Place")
				.addEvents(CollectionUtils.array(BlockPlaceEvent.class, PlayerBucketEmptyEvent.class, HangingPlaceEvent.class))
				.addPatterns("[block] (plac(e|ing)|build[ing]) [[of] %-itemtypes/blockdatas%]")
				.addDescription("Called when a player places a block.")
				.addExample("""
					on place:
					on place of a furnace, crafting table or chest:
					on break of chest[type=right] or chest[type=left]
					""")
				.addSince("1.0, 2.6 (BlockData support)")
				.supplier(EvtBlock::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtBlock.class, "Block Fade")
				.addEvent(BlockFadeEvent.class)
				.addPatterns("[block] fad(e|ing) [[of] %-itemtypes/blockdatas%]")
				.addDescription("Called when a block 'fades away', e.g. ice or snow melts.")
				.addExample("""
					on fade of snow or blue ice:
					on fade of snow[layers=2]:
					""")
				.addSince("1.0, 2.6 (BlockData support)")
				.supplier(EvtBlock::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtBlock.class, "Form")
				.addEvent(BlockFormEvent.class)
				.addPatterns("[block] form[ing] [[of] %-itemtypes/blockdatas%]")
				.addDescription("Called when a block is created, but not by a player, e.g. snow forms due to snowfall, water freezes in cold biomes. This isn't called when block spreads (mushroom growth, water physics etc.), as it has its own event (see <a href='#spread'>spread event</a>).")
				.addExample("on form of snow:")
				.addSince("1.0, 2.6 (BlockData support)")
				.supplier(EvtBlock::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtBlock.class, "Block Drop")
				.addEvent(BlockDropItemEvent.class)
				.addPatterns("block drop[ping] [[of] %-itemtypes/blockdatas%]")
				.addDescription(
					"""
					Called when a block broken by a player drops something.	
					<ul>
					<li>event-player: The player that broke the block</li>
					<li>past event-block: The block that was broken</li>
					<li>event-block: The block after being broken</li>
					<li>event-items (or drops): The drops of the block</li>
					<li>event-entities: The entities of the dropped items</li>
					</ul>
					
					If the breaking of the block leads to others being broken, such as torches, they will appear in "event-items" and "event-entities".
					""")
				.addExample(
					"""
					on block drop:
						broadcast event-player
						broadcast past event-block
						broadcast event-block
						broadcast event-items
						broadcast event-entities
					on block drop of oak log:
					""")
				.addSince("2.10")
				.supplier(EvtBlock::new)
				.build()
		);

	}

	@Nullable
	private Literal<Object> types;

	private boolean mine = false;

	@Override
	public boolean init(final Literal<?>[] args, final int matchedPattern, final ParseResult parser) {
		types = args.length > 0 ? (Literal<Object>) args[0] : null;
		mine = parser.mark == 1;
		return true;
	}

	@Override
	public boolean check(final Event event) {
		if (mine && event instanceof BlockBreakEvent) {
			if (((BlockBreakEvent) event).getBlock().getDrops(((BlockBreakEvent) event).getPlayer().getItemInHand()).isEmpty())
				return false;
		}
		if (types == null)
			return true;

		ItemType item;
		BlockData blockData = null;

		if (event instanceof BlockFormEvent blockFormEvent) {
			BlockState newState = blockFormEvent.getNewState();
			item = new ItemType(newState.getBlockData());
			blockData = newState.getBlockData();
		} else if (event instanceof BlockDropItemEvent blockDropItemEvent) {
			Block block = blockDropItemEvent.getBlock();
			item = new ItemType(block);
			blockData = block.getBlockData();
		} else if (event instanceof BlockEvent blockEvent) {
			Block block = blockEvent.getBlock();
			item = new ItemType(block);
			blockData = block.getBlockData();
		} else if (event instanceof PlayerBucketFillEvent playerBucketFillEvent) {
			Block block = playerBucketFillEvent.getBlockClicked();
			item = new ItemType(block);
			blockData = block.getBlockData();
		} else if (event instanceof PlayerBucketEmptyEvent playerBucketEmptyEvent) {
			item = new ItemType(playerBucketEmptyEvent.getItemStack());
		} else if (event instanceof HangingEvent hangingEvent) {
			final EntityData<?> d = EntityData.fromEntity(hangingEvent.getEntity());
			return types.check(event, o -> {
				if (o instanceof ItemType)
					return Relation.EQUAL.isImpliedBy(DefaultComparators.entityItemComparator.compare(d, (ItemType) o));
				return false;
			});
		} else {
			return true;
		}

		final ItemType itemF = item;
		BlockData finalBlockData = blockData;

		return types.check(event, o -> {
			if (o instanceof ItemType)
				return ((ItemType) o).isSupertypeOf(itemF);
			else if (o instanceof BlockData && finalBlockData != null)
				return finalBlockData.matches((BlockData) o);
			return false;
		});
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "break/place/burn/fade/form/drop of " + Classes.toString(types);
	}

}
