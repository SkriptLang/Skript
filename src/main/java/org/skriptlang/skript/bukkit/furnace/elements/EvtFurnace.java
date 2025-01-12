package org.skriptlang.skript.bukkit.furnace.elements;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Classes;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.FurnaceStartSmeltEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.registration.BukkitRegistryKeys;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EvtFurnace extends SkriptEvent {

	public static void register(SyntaxRegistry registry) {
		registry.register(BukkitRegistryKeys.EVENT, BukkitSyntaxInfos.Event
			.builder(EvtFurnace.class, "Smelt")
			.addEvent(FurnaceSmeltEvent.class)
			.addPatterns(
				"[furnace] [ore] smelt[ed|ing] [of %-itemtypes%]",
				"[furnace] smelt[ed|ing] of ore"
			)
			.addDescription("Called when a furnace smelts an item in its <a href='expressions.html#ExprFurnaceSlot'>input slot</a>.")
			.addExamples(
				"on smelt:",
					"\tclear the smelted item",
				"on smelt of raw iron:",
					"\tbroadcast smelted item",
					"\tset the smelted item to iron block"
			)
			.since("1.0, 2.10 (specific item)")
			.build()
		);

		registry.register(BukkitRegistryKeys.EVENT, BukkitSyntaxInfos.Event
			.builder(EvtFurnace.class, "Fuel Burn")
			.addEvent(FurnaceBurnEvent.class)
			.addPattern("[furnace] fuel burn[ing] [of %-itemtypes%]")
			.addDescription("Called when a furnace burns an item from its <a href='expressions.html#ExprFurnaceSlot'>fuel slot</a>.")
			.addExamples(
				"on fuel burning:",
					"\tbroadcast fuel burned",
					"\tif burned fuel is coal:",
						"\t\tadd 20 seconds to burn time"
			)
			.since("1.0, 2.10 (specific item)")
			.build()
		);

		registry.register(BukkitRegistryKeys.EVENT, BukkitSyntaxInfos.Event
			.builder(EvtFurnace.class, "Furnace Item Extract")
			.addEvent(FurnaceExtractEvent.class)
			.addPattern("furnace [item] extract[ion] [of %-itemtypes%]")
			.addDescription("Called when a player takes any item out of the furnace.")
			.addExamples(
				"on furnace extract:",
				"\tif event-items is an iron ingot:",
				"\t\tremove event-items from event-player's inventory"
			)
			.since("2.10")
			.build()
		);

		registry.register(BukkitRegistryKeys.EVENT, BukkitSyntaxInfos.Event
			.builder(EvtFurnace.class, "Smart Smelt")
			.addEvent(FurnaceStartSmeltEvent.class)
			.addPatterns(
				"[furnace] start [of] smelt[ing] [[of] %-itemtypes%]",
				"[furnace] smelt[ing] start [of %-itemtypes%]"
			)
			.addDescription("Called when a furnace starts smelting an item in its ore slot.")
			.addExamples(
				"on smelting start:",
					"\tif the smelting item is raw iron:",
						"\t\tset total cook time to 1 second",
				"",
				"on smelting start of raw iron:",
					"\tadd 20 seconds to total cook time"
			)
			.since("2.10")
			.build()
		);
	}

	private @Nullable Literal<ItemType> types;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Literal<?>[] exprs, int matchedPattern, ParseResult parseResult) {
		if (exprs[0] != null)
			types = (Literal<ItemType>) exprs[0];
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (types == null)
			return true;

		ItemType item;

		if (event instanceof FurnaceSmeltEvent smeltEvent) {
			item = new ItemType(smeltEvent.getSource());
		} else if (event instanceof FurnaceBurnEvent burnEvent) {
			item = new ItemType(burnEvent.getFuel());
		} else if (event instanceof FurnaceExtractEvent extractEvent) {
			item = new ItemType(extractEvent.getItemType());
		} else if (event instanceof FurnaceStartSmeltEvent startEvent) {
			item = new ItemType(startEvent.getSource());
		} else {
			assert false;
			return false;
		}

		return types.check(event, itemType -> itemType.isSupertypeOf(item));
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		String result = "";
		if (event instanceof FurnaceSmeltEvent) {
			result = "smelt";
		} else if (event instanceof FurnaceBurnEvent) {
			result = "burn";
		} else if (event instanceof FurnaceExtractEvent) {
			result = "extract";
		} else if (event instanceof FurnaceStartSmeltEvent) {
			result = "start smelt";
		} else {
			throw new IllegalStateException("Unexpected event: " + event);
		}
		return result + " of " + Classes.toString(types);
	}

}
