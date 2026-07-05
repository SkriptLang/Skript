package org.skriptlang.skript.bukkit.block.elements;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.util.SimpleEvent;
import ch.njol.skript.util.BlockStateBlock;
import ch.njol.skript.util.BlockUtils;
import ch.njol.skript.util.Direction;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.*;
import org.bukkit.inventory.ItemStack;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue.Time;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class BlockEvents {

	public static void register(SyntaxRegistry syntaxRegistry, EventValueRegistry eventValueRegistry) {

		// Block Event Values

		eventValueRegistry.register(EventValue.builder(BlockEvent.class, Block.class)
			.getter(BlockEvent::getBlock)
			.build());

		eventValueRegistry.register(EventValue.builder(BlockEvent.class, World.class)
			.getter(event -> event.getBlock().getWorld())
			.build());

		// Note: The event's location is at the entity in block events that have an entity event value
		eventValueRegistry.register(EventValue.builder(BlockEvent.class, Location.class)
			.getter(event -> BlockUtils.getLocation(event.getBlock()))
			.build());

		//
		// Block Events
		//

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Can Build Check")
			.addEvent(BlockCanBuildEvent.class)
			.addPatterns("[block] can build check")
			.addDescription("""
				Called when a player right clicks on a block while holding a block or a placeable item.
				Cancelling this will prevent the block from being placed.
				Note: The <a href='#ExprDurability'>data value</a> of the block to be placed is not available in this event, only its <a href='#ExprIdOf'>ID</a>.
				""")
			.addExample("""
				on block can build check:
				    broadcast "We gotta do a check!"
				""")
			.addSince("1.0 (basic), 2.0 ([un]cancellable)")
			.supplier(() -> new SimpleEvent("block can build check"))
			.build());

		eventValueRegistry.register(EventValue.builder(BlockCanBuildEvent.class, Player.class)
			.getter(BlockCanBuildEvent::getPlayer)
			.build());

		eventValueRegistry.register(EventValue.builder(BlockCanBuildEvent.class, Block.class)
			.getter(BlockCanBuildEvent::getBlock)
			.time(Time.PAST)
			.build());

		eventValueRegistry.register(EventValue.builder(BlockCanBuildEvent.class, Block.class)
			.getter(event -> {
				BlockState state = event.getBlock().getState();
				state.setType(event.getMaterial());
				return new BlockStateBlock(state, true);
			})
			.build());

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Block Damage")
			.addEvent(BlockDamageEvent.class)
			.addPatterns("block damag(ing|e)", "[player] damag(ing|e) [a] block")
			.addDescription("""
				Called when a player starts to break a block.
				Most times you can just use the click event for this.
				""")
			.addExample("""
				on player damaging a block:
				    if event-block is tagged with minecraft tag "logs":
				        send "You cannot break the holy log!" to player
				""")
			.addSince("1.0, INSERT VERSION (added new pattern)")
			.supplier(() -> new SimpleEvent("block damage"))
			.build());

		eventValueRegistry.register(EventValue.builder(BlockDamageEvent.class, Player.class)
			.getter(BlockDamageEvent::getPlayer)
			.build());

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Block Flow")
			.addEvent(BlockFromToEvent.class)
			.addPatterns("[block] flow[ing]", "block mov(e|ing)")
			.addDescription("""
				Called when a blocks flows or teleports to another block.
				This event applies to water, lava and dragon eggs.
				""")
			.addExample("""
				on block flow:
				    if event-block is water:
				        broadcast "Build more dams! It's starting to get wet in here!"
				""")
			.addSince("1.0")
			.supplier(() -> new SimpleEvent("block flow"))
			.build());

		eventValueRegistry.register(EventValue.builder(BlockFromToEvent.class, Block.class)
			.getter(BlockFromToEvent::getToBlock)
			.time(Time.FUTURE)
			.build());

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Block Flow")
			.addEvent(BlockIgniteEvent.class)
			.addPatterns("[block] ignit(e|ion)")
			.addDescription("""
				Called when a block starts burning, i.e. a fire block is placed next to it and this block is flammable.
				Use <a href='#burn'>burn event</a> for when the block is about do be destroyed by the fire.
				""")
			.addExample("""
				on block ignite:
				    if event-block is a ladder:
				        cancel event
				        broadcast "No ladders were harmed in the making of this film."
				""")
			.addSince("1.0")
			.supplier(() -> new SimpleEvent("block ignite"))
			.build());

		eventValueRegistry.register(EventValue.builder(BlockIgniteEvent.class, Player.class)
			.getter(BlockIgniteEvent::getPlayer)
			.build());

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Block Physics")
			.addEvent(BlockPhysicsEvent.class)
			.addPatterns("[block] physics")
			.addDescription("""
				Called when a physics check is done on a block.
				Cancelling this event will prevent the physics from effecting the block.
				Note that this event is called extremely often and may cause performance issues.
				""")
			.addExample("""
				on block physics:
				    if event-block is sand:
				        cancel event
				        add 1 to {sand}
				        send actionbar "This code has stopped %{sand}%x sand from falling!" to all players
				""")
			.addSince("1.4.6")
			.supplier(() -> new SimpleEvent("block physics"))
			.build());

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Piston Extend")
			.addEvent(BlockPistonExtendEvent.class)
			.addPatterns("piston extend[ing]")
			.addDescription("Called when a piston is about to extend.")
			.addExample("""
				on piston extend:
				    broadcast "A piston is extending!"
				""")
			.addSince("1.0")
			.supplier(() -> new SimpleEvent("piston extend"))
			.build());

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Piston Retract")
			.addEvent(BlockPistonRetractEvent.class)
			.addPatterns("piston retract[ing]")
			.addDescription("Called when a piston is about to retract.")
			.addExample("""
				on piston retract:
				    broadcast "A piston is retracting!"
				""")
			.addSince("1.0")
			.supplier(() -> new SimpleEvent("piston retract"))
			.build());

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Redstone")
			.addEvent(BlockRedstoneEvent.class)
			.addPatterns("redstone [current] [chang(e|ing)]")
			.addDescription("Called when the redstone current of a block changes.")
			.addExample("""
				on redstone change:
				    broadcast "Someone is using redstone!" to console
				""")
			.addSince("1.0")
			.supplier(() -> new SimpleEvent("redstone"))
			.build());

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Block Spread")
			.addEvent(BlockSpreadEvent.class)
			.addPatterns("[block] spread[ing]")
			.addDescription("""
				Called when a new block <a href='#form'>forms</a> as a result of a block that can spread,\s
				e.g. water or mushrooms.
				""")
			.addExample("""
				on block spread:
				    broadcast "its spreading!"
				""")
			.addSince("1.0")
			.supplier(() -> new SimpleEvent("block spread"))
			.build());

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Block Fertilize")
			.addEvent(BlockFertilizeEvent.class)
			.addPatterns("[block] fertilize")
			.addDescription("Called when a player fertilizes blocks.")
			.addExample("""
				on block fertilize:
				    broadcast "Ew.. %size of event-blocks% got fertilized.."
				""")
			.addSince("2.5")
			.supplier(() -> new SimpleEvent("block fertilize"))
			.build());

		eventValueRegistry.register(EventValue.builder(BlockFertilizeEvent.class, Player.class)
			.getter(BlockFertilizeEvent::getPlayer)
			.build());

		eventValueRegistry.register(EventValue.builder(BlockFertilizeEvent.class, Block[].class)
			.getter(event -> event.getBlocks().stream()
				.map(BlockState::getBlock)
				.toArray(Block[]::new)
			)
			.build());

		//
 		// Block specific events (e.g. LeavesDecayEvent)
 		//

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Leaves Decay")
			.addEvent(LeavesDecayEvent.class)
			.addPatterns("leaves decay[ing]")
			.addDescription("Called when a leaf block decays due to not being connected to a tree.")
			.addExample("""
				on leaves decay:
				    broadcast "Its beginning to look a lot like fall.."
				""")
			.addSince("1.0")
			.supplier(() -> new SimpleEvent("leaves decay"))
			.build());

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Sign Change")
			.addEvent(SignChangeEvent.class)
			.addPatterns("sign (chang[e]|edit)[ing]", "[player] (chang[e]|edit)[ing] [a] sign")
			.addDescription("Called when a player finishes editing a sign after placing it or right clicking on it if its not waxed.")
			.addExample("""
				on player changing a sign:
				    set line 1 to "It must be a sign!"
				""")
			.addSince("1.0")
			.supplier(() -> new SimpleEvent("sign change"))
			.build());

		eventValueRegistry.register(EventValue.builder(SignChangeEvent.class, Player.class)
			.getter(SignChangeEvent::getPlayer)
			.build());

		eventValueRegistry.register(EventValue.builder(SignChangeEvent.class, Component[].class)
			.getter( event -> event.lines().toArray(new Component[0]))
			.build());

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Sponge Absorb")
			.addEvent(SpongeAbsorbEvent.class)
			.addPatterns("sponge absorb[ing]")
			.addDescription("Called when a sponge absorbs blocks.")
			.addExample("""
				on sponge absorb:
				    loop absorbed blocks:
				        broadcast "%loop-block% was absorbed by a sponge!"
				""")
			.addSince("2.5")
			.supplier(() -> new SimpleEvent("sponge absorb"))
			.build());

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Bell Ring")
			.addEvent(BellRingEvent.class)
			.addPatterns("bell ring[ing]")
			.addDescription("Called when a bell is rung.")
			.addExample("""
				on bell ring:
				    send "<gold>Ding-dong!" to (all players in radius 10 of event-block)
				""")
			.addSince("2.9.0")
			.supplier(() -> new SimpleEvent("bell ring"))
			.build());

		eventValueRegistry.register(EventValue.builder(BellRingEvent.class, Entity.class)
			.getter(BellRingEvent::getEntity)
			.build());

		eventValueRegistry.register(EventValue.builder(BellRingEvent.class, Direction.class)
			.getter(event -> new Direction(event.getDirection(), 1))
			.build());

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Bell Resonate")
			.addEvent(BellResonateEvent.class)
			.addPatterns("bell resonat(e|ing)")
			.addDescription("Called when a bell resonates, highlighting nearby raiders.")
			.addExample("""
				on bell resonate:
				    send "<red>There are raiders nearby!" to (all players in radius 32 of event-block)
				""")
			.addSince("2.9.0")
			.supplier(() -> new SimpleEvent("bell resonate"))
			.build());

		eventValueRegistry.register(EventValue.builder(BellResonateEvent.class, Entity[].class)
			.getter(event -> event.getResonatedEntities().toArray(new LivingEntity[0]))
			.build());

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Vault Display Item")
			.addEvent(VaultDisplayItemEvent.class)
			.addPatterns("vault display[ing] item")
			.addDescription("Called when a vault in a trial chamber is about to display an item.")
			.addExample("""
				on vault display item:
				    set event-item to netherite ingot
				""")
			.addSince("2.12")
			.supplier(() -> new SimpleEvent("vault display item"))
			.build());

		eventValueRegistry.register(EventValue.builder(VaultDisplayItemEvent.class, ItemStack.class)
			.getter(VaultDisplayItemEvent::getDisplayItem)
			.registerChanger(ChangeMode.SET, VaultDisplayItemEvent::setDisplayItem)
			.build());
	}

}
