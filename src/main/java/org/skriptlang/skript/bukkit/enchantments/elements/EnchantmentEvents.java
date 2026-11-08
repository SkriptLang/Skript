package org.skriptlang.skript.bukkit.enchantments.elements;

import ch.njol.skript.lang.util.SimpleEvent;
import ch.njol.skript.util.EnchantmentType;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.inventory.ItemStack;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EnchantmentEvents {

	public static void register(SyntaxRegistry syntaxRegistry, EventValueRegistry eventValueRegistry) {
		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Enchant Prepare")
			.addEvent(PrepareItemEnchantEvent.class)
			.addPatterns("[item] enchant prepare")
			.addDescription("""
				Called when a player puts an item into an enchantment table.
				This event may be called multiple times.
				See <a href='#ExprEnchantEventsEnchantItem'>enchant item expression</a> on how to get the enchant item.
				""")
			.addExample("""
				on item enchant prepare:
					set enchant offer 1 to sharpness 1
					set the cost of enchantment offer 1 to 10 levels
				""")
			.addSince("2.5")
			.supplier(() -> new SimpleEvent("item enchant prepare"))
			.build());

		eventValueRegistry.register(EventValue.builder(PrepareItemEnchantEvent.class, Player.class)
			.getter(PrepareItemEnchantEvent::getEnchanter)
			.build());

		eventValueRegistry.register(EventValue.builder(PrepareItemEnchantEvent.class, ItemStack.class)
			.getter(PrepareItemEnchantEvent::getItem)
			.build());

		eventValueRegistry.register(EventValue.builder(PrepareItemEnchantEvent.class, Block.class)
			.getter(PrepareItemEnchantEvent::getEnchantBlock)
			.build());

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Item Enchant")
			.addEvent(EnchantItemEvent.class)
			.addPatterns("[item] enchant")
			.addDescription("""
				Called when a player successfully enchants an item.
				See <a href='#ExprEnchantEventsEnchantItem'>enchant item expression</a> on how to get the enchanted item.
				""")
			.addExample("""
				on enchant:
					if the clicked button is 1:
						set the applied enchantments to (sharpness 10 and unbreaking 10)
				""")
			.addSince("2.5")
			.supplier(() -> new SimpleEvent("item enchant"))
			.build());

		eventValueRegistry.register(EventValue.builder(EnchantItemEvent.class, Player.class)
			.getter(EnchantItemEvent::getEnchanter)
			.build());

		eventValueRegistry.register(EventValue.builder(EnchantItemEvent.class, ItemStack.class)
			.getter(EnchantItemEvent::getItem)
			.build());

		eventValueRegistry.register(EventValue.builder(EnchantItemEvent.class, EnchantmentType[].class)
			.getter(event -> event.getEnchantsToAdd().entrySet().stream()
				.map(entry -> new EnchantmentType(entry.getKey(), entry.getValue()))
				.toArray(EnchantmentType[]::new))
			.build());

		eventValueRegistry.register(EventValue.builder(EnchantItemEvent.class, Block.class)
			.getter(EnchantItemEvent::getEnchantBlock)
			.build());
	}

}
