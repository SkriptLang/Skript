package org.skriptlang.skript.bukkit.block.elements.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import com.destroystokyo.paper.event.block.AnvilDamagedEvent;
import org.bukkit.event.Event;
import org.bukkit.event.block.*;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
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
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtBlock.class, "Leaves Decay")
				.addPatterns("leaves decay[ing]")
				.addDescription("Called when a leaf block decays due to not being connected to a tree.")
				.addSince("1.0")
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
								broadcast "%loop-block% was absorbed by a sponge"!
						""")
				.addSince("2.5")
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
					.build()
			);
		}

		if (Skript.classExists("org.bukkit.event.block.BlockBreakEvent")) {
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
					.build()
			);
		}
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
		return "block event";
	}

}
