package org.skriptlang.skript.bukkit.furnace.elements;

import ch.njol.skript.Skript;
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

public class EvtFurnace extends SkriptEvent {

	static {
		Skript.registerEvent("Smelt", EvtFurnace.class, FurnaceSmeltEvent.class,
				"[furnace] [ore] smelt[ed|ing] [of %-itemtypes%]",
				"[furnace] smelt[ed|ing] of ore")
			.description("Called when a furnace smelts an item in its <a href='expressions.html#ExprFurnaceSlot'>input slot</a>.")
			.examples(
				"on smelt:",
					"\tclear the smelted item",
				"on smelt of raw iron:",
					"\tbroadcast smelted item",
					"\tset the smelted item to iron block"
			)
			.since("1.0, 2.10 (specific item)");

		Skript.registerEvent("Fuel Burn", EvtFurnace.class, FurnaceBurnEvent.class, "[furnace] fuel burn[ing] [of %-itemtypes%]")
			.description("Called when a furnace burns an item from its <a href='expressions.html#ExprFurnaceSlot'>fuel slot</a>.")
			.examples(
				"on fuel burning:",
					"\tbroadcast fuel burned",
					"\tif burned fuel is coal:",
						"\t\tadd 20 seconds to burn time"
			)
			.since("1.0, 2.10 (specific item)");

		Skript.registerEvent("Furnace Item Extract", EvtFurnace.class, FurnaceExtractEvent.class, "furnace [item] extract[ion] [of %-itemtypes%]")
			.description("Called when a player takes any item out of the furnace.")
			.examples(
				"on furnace extract:",
					"\tif event-items is an iron ingot:",
						"\t\tremove event-items from event-player's inventory"
			)
			.since("2.10");

		Skript.registerEvent("Start Smelt", EvtFurnace.class, FurnaceStartSmeltEvent.class,
			"[furnace] start [of] smelt[ing] [[of] %-itemtypes%]",
			"[furnace] smelt[ing] start [of %-itemtypes%]")
			.description("Called when a furnace starts smelting an item in its ore slot.")
			.examples(
				"on smelting start:",
					"\tif the smelting item is raw iron:",
						"\t\tset total cook time to 1 second",
				"on smelting start of raw iron:",
					"\tadd 20 seconds to total cook time"
			)
			.since("2.10");
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
