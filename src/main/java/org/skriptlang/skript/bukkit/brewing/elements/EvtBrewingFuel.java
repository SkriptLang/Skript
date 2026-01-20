package org.skriptlang.skript.bukkit.brewing.elements;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.registrations.EventValues;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.BrewingStandFuelEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EvtBrewingFuel extends SkriptEvent {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtBrewingFuel.class, "Brewing Fuel")
				.addEvent(BrewingStandFuelEvent.class)
				.addPatterns(
					"brew[ing [stand]] consum(e|ing) fuel [of %-itemtypes%]",
					"brew[ing [stand]] fuel consumption [of %-itemtypes%]"
				)
				.addDescription("Called when a brewing stand is about to use an item to increase its fuel level.")
				.addExample("""
					on brewing consume fuel:
						prevent the brewing stand from consuming fuel
					on brewing fuel consumption of blaze powder:
					""")
				.addSince("2.13")
				.supplier(EvtBrewingFuel::new)
				.build()
		);

		EventValues.registerEventValue(BrewingStandFuelEvent.class, ItemStack.class, BrewingStandFuelEvent::getFuel);
	}

	private Literal<ItemType> items;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		//noinspection unchecked
		items = (Literal<ItemType>) args[0];
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (!(event instanceof BrewingStandFuelEvent brewingStandFuelEvent))
			return false;
		if (items == null)
			return true;

		ItemStack itemStack = brewingStandFuelEvent.getFuel();
		for (ItemType itemType : items.getArray()) {
			if (itemType.isOfType(itemStack))
				return true;
		}
		return false;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append("brewing stand fuel consumption");
		if (items != null)
			builder.append("of", items);
		return builder.toString();
	}

}
