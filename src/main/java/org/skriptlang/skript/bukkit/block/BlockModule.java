package org.skriptlang.skript.bukkit.block;

import ch.njol.skript.lang.util.SimpleEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockDamageAbortEvent;
import org.bukkit.inventory.ItemStack;
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

		moduleRegistry(addon).register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Block Damage Abort")
			.addEvent(BlockDamageAbortEvent.class)
			.addPatterns(
				"[player] (interrupt|stop|abort[ing]) (damag(e|ing)|break(ing)) [a] block",
				"block damage (interrupt|abort|stop)",
				"block damage being (interrupted|aborted|stopped)"
			)
			.addDescription("Called when a player stops breaking a block.")
			.addExample("""
				on stop breaking block:
					send "Hey! You have to finish what you started!" to player
				""")
			.addSince("INSERT VERSION")
			.supplier(() -> new SimpleEvent("block damage abort"))
			.build());

		EventValueRegistry eventValueRegistry = addon.registry(EventValueRegistry.class);


		eventValueRegistry.register(EventValue.simple(BlockDamageAbortEvent.class, Player.class, BlockDamageAbortEvent::getPlayer));

		eventValueRegistry.register(EventValue.simple(BlockDamageAbortEvent.class, ItemStack.class, BlockDamageAbortEvent::getItemInHand));
	}

	@Override
	public String name() {
		return "block";
	}

}

