package org.skriptlang.skript.bukkit.misc.elements.events;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser;
import org.bukkit.event.Event;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EvtEnchantItem extends SkriptEvent {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtEnchantItem.class, "Enchant Prepare")
				.addEvent(PrepareItemEnchantEvent.class)
				.addPatterns("[item] enchant prepare")
				.addDescription("""
					Called when a player puts an item into enchantment table. This event may be called multiple times.
					 To get the enchant item, see the <a href='#ExprEnchantEventsEnchantItem'>enchant item expression</a>
					""")
				.addExample("""
					on enchant prepare:
						set enchant offer 1 to sharpness 1
						set the cost of enchant offer 1 to 10 levels
					""")
				.addSince("2.5")
				.supplier(EvtEnchantItem::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtEnchantItem.class, "Enchant")
				.addEvent(EnchantItemEvent.class)
				.addPatterns("[item] enchant")
				.addDescription("Called when a player successfully enchants an item.",
					" To get the enchanted item, see the <a href='#ExprEnchantEventsEnchantItem'>enchant item expression</a>")
				.addExample("""
					on enchant:
						if the clicked button is 1: # offer 1
							set the applied enchantments to sharpness 10 and unbreaking 10
					""")
				.addSince("2.5")
				.supplier(EvtEnchantItem::new)
				.build()
		);
	}

	@Override
	public boolean init(Literal<?>[] exprs, int matchedPattern, SkriptParser.ParseResult parseResult) {
		return true;
	}

	@Override
	public boolean check(Event event) {
		return true;
	}

	@Override
	public String toString(Event event, boolean debug) { return "enchant event"; }
}
