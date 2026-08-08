package org.skriptlang.skript.bukkit.block;

import ch.njol.skript.lang.util.SimpleEvent;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockDamageAbortEvent;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.block.furnace.FurnaceModule;
import org.skriptlang.skript.bukkit.block.sign.SignModule;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;

import java.util.List;

public class BlockModule extends HierarchicalAddonModule {

	public BlockModule(AddonModule parentModule) {
		super(parentModule);
	}

	@Override
	public Iterable<AddonModule> children() {
		return List.of(
			new FurnaceModule(this),
			new SignModule(this)
		);
	}

	@Override
	public void loadSelf(SkriptAddon addon) {

		// Register the event
		moduleRegistry(addon).register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Block Damage Abort")
			.addEvent(BlockDamageAbortEvent.class)
			.addPatterns("(stop|abort) [of] block (break[ing]|damag(e|ing))")
			.addPatterns("block (stop|abort) (break[ing]|damag(e|ing))")
			.addDescription("Called when a player stops breaking a block.")
			.addExample("""
                on block stop breaking:
                    send "Hey! You have to finish what you started!"
                """)
			.addSince("INSERT VERSION")
			.supplier(() -> new SimpleEvent("block damage abort"))
			.build());

		// Register values for said event
		EventValueRegistry eventValueRegistry = addon.registry(EventValueRegistry.class);
		eventValueRegistry.register(EventValue.builder(BlockDamageAbortEvent.class, Player.class)
			.getter(BlockDamageAbortEvent::getPlayer)
			.build());

		eventValueRegistry.register(EventValue.builder(BlockDamageAbortEvent.class, Block.class)
			.getter(BlockDamageAbortEvent::getBlock)
			.build());
	}

	@Override
	public String name() {
		return "block";
	}

}
