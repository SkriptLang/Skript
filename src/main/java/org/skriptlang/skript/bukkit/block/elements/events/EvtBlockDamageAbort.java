package org.skriptlang.skript.bukkit.block.elements.events;

import ch.njol.skript.lang.util.SimpleEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockDamageAbortEvent;
import org.bukkit.inventory.ItemStack;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EvtBlockDamageAbort extends SimpleEvent {

	public EvtBlockDamageAbort() {
		super("block damage abort");
	}

	public static void register(SyntaxRegistry syntaxRegistry, EventValueRegistry eventValueRegistry) {
		syntaxRegistry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtBlockDamageAbort.class, "Block Damage Abort")
				.supplier(EvtBlockDamageAbort::new)
				.addEvent(BlockDamageAbortEvent.class)
				.addPattern("block damage abort")
				.addDescription("Called when a player stops damaging a block before it breaks.")
				.addExample("""
					on block damage abort:
						send "You stopped damaging %event-block% with %event-item%." to event-player
					""")
				.addSince("2.16")
				.build()
		);

		eventValueRegistry.register(EventValue.simple(
			BlockDamageAbortEvent.class,
			Player.class,
			BlockDamageAbortEvent::getPlayer
		));
		eventValueRegistry.register(EventValue.simple(
			BlockDamageAbortEvent.class,
			ItemStack.class,
			BlockDamageAbortEvent::getItemInHand
		));
	}

}
