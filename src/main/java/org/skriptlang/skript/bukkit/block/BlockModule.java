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
import org.skriptlang.skript.registration.SyntaxRegistry;

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
		EventValueRegistry eventValueRegistry = addon.registry(EventValueRegistry.class);
		SyntaxRegistry syntaxRegistry = moduleRegistry(addon);

		syntaxRegistry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Block Damage Abort")
				.addEvent(BlockDamageAbortEvent.class)
				.supplier(() -> new SimpleEvent("block damage abort"))
				.addPattern("block damage abort")
				.addDescription("Called when a player stops damaging a block before it breaks.")
				.addExample("""
					on block damage abort:
						send "You stopped damaging %event-block% with %event-item%." to event-player
					""")
				.addSince("INSERT VERSION")
				.build()
		);

		eventValueRegistry.register(EventValue.builder(BlockDamageAbortEvent.class, Player.class)
			.getter(BlockDamageAbortEvent::getPlayer)
			.build());
		eventValueRegistry.register(EventValue.builder(BlockDamageAbortEvent.class, ItemStack.class)
			.getter(BlockDamageAbortEvent::getItemInHand)
			.build());
	}

	@Override
	public String name() {
		return "block";
	}

}
