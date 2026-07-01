package org.skriptlang.skript.bukkit.block.furnace.elements.events;

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
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EvtFurnace extends SkriptEvent {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtFurnace.class, "Smelt")
				.addEvent(FurnaceSmeltEvent.class)
				.addPatterns(
					"[furnace] [ore] smelt[ed|ing] [of %-itemtypes%]",
					"[furnace] smelt[ed|ing] of ore"
				)
				.addDescription("Called when a furnace smelts an item in its <a href='#ExprFurnaceSlot'>input slot</a>.")
				.addExample("""
					on smelt:
						clear the smelted item
					""")
				.addExample("""
					on smelt of raw iron:
						broadcast smelted item
						set the smelted item to iron block
					""")
				.addSince("1.0, 2.10 (specific item)")
				.supplier(EvtFurnace::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtFurnace.class, "Fuel Burn")
				.addEvent(FurnaceBurnEvent.class)
				.addPatterns("[furnace] fuel burn[ing] [of %-itemtypes%]")
				.addDescription("Called when a furnace burns an item from its <a href='#ExprFurnaceSlot'>fuel slot</a>.")
				.addExample("""
					on fuel burning:
						broadcast fuel burned
						if burned fuel is coal:
							add 20 seconds to burn time
					""")
				.addSince("1.0, 2.10 (specific item)")
				.supplier(EvtFurnace::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtFurnace.class, "Furnace Item Extract")
				.addEvent(FurnaceExtractEvent.class)
				.addPatterns("furnace [item] extract[ion] [of %-itemtypes%]")
				.addDescription("Called when a player takes any item out of the furnace.")
				.addExample("""
					on furnace extract:
						if event-items is an iron ingot:
							remove event-items from event-player's inventory
					""")
				.addSince("2.10")
				.supplier(EvtFurnace::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtFurnace.class, "Start Smelt")
				.addEvent(FurnaceStartSmeltEvent.class)
				.addPatterns(
					"[furnace] start [of] smelt[ing] [[of] %-itemtypes%]",
					"[furnace] smelt[ing] start [of %-itemtypes%]"
				)
				.addDescription("Called when a furnace starts smelting an item in its ore slot.")
				.addExample("""
					on smelting start:
						if the smelting item is raw iron:
							set total cook time to 1 second
					""")
				.addExample("""
					on smelting start of raw iron:
						add 20 seconds to total cook time
					""")
				.addSince("2.10")
				.supplier(EvtFurnace::new)
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
		// this is bad and should be 4 separate event classes in the future
		Class<? extends Event> eventClass = getEventClasses()[0];
		String result;
		if (eventClass == FurnaceSmeltEvent.class) {
			result = "smelt";
		} else if (eventClass == FurnaceBurnEvent.class) {
			result = "burn";
		} else if (eventClass == FurnaceExtractEvent.class) {
			result = "extract";
		} else if (eventClass == FurnaceStartSmeltEvent.class) {
			result = "start smelt";
		} else {
			throw new IllegalStateException("Unexpected event: " + event);
		}
		return result + " of " + Classes.toString(types);
	}

}
