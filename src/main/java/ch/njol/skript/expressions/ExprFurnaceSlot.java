package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.slot.InventorySlot;
import ch.njol.skript.util.slot.Slot;
import ch.njol.util.Kleenean;
import ch.njol.util.Math2;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Furnace;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.FurnaceStartSmeltEvent;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Name("Furnace Slot")
@Description({
	"A slot of a furnace, i.e. either the ore, fuel or result slot."
})
@Examples({
	"set the fuel slot of the clicked block to a lava bucket",
	"set the block's ore slot to 64 iron ore",
	"clear the result slot of the block",
	"on smelt:",
		"\tif the fuel slot is charcoal:",
			"\t\tadd 5 seconds to the burn time"
})
@Events({"smelt", "fuel burn"})
@Since("1.0, 2.8.0 (syntax rework)")
public class ExprFurnaceSlot extends SimpleExpression<Slot> {

	private enum FurnaceSlot {
		INPUT("(ore|input)", "input"),
		FUEL("fuel", "fuel"),
		OUTPUT("(result|output)", "output");

		private String pattern, toString;

		FurnaceSlot(String pattern, String toString) {
			this.pattern = pattern;
			this.toString = toString;
		}
	}

	private static final FurnaceSlot[] furnaceSlots = FurnaceSlot.values();

	static {
		String[] patterns = new String[furnaceSlots.length * 2];
		for (FurnaceSlot slot : furnaceSlots) {
			patterns[2 * slot.ordinal()] = "[the] " + slot.pattern + " slot[s] [of %blocks%]";
			patterns[2 * slot.ordinal() + 1] = "%blocks%'[s] " + slot.pattern + " slot[s]";
		}
		Skript.registerExpression(ExprFurnaceSlot.class, Slot.class, ExpressionType.PROPERTY, patterns);
	}


	private @Nullable Expression<Block> blocks;
	private FurnaceSlot selectedSlot;
	private boolean isEvent;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		selectedSlot = furnaceSlots[(int) Math2.floor(matchedPattern / 2)];
		if (exprs[0] != null) {
			blocks = (Expression<Block>) exprs[0];
		} else {
			if (!getParser().isCurrentEvent(FurnaceBurnEvent.class, FurnaceStartSmeltEvent.class, FurnaceExtractEvent.class, FurnaceSmeltEvent.class)) {
				Skript.error("There's no furnace in a " + getParser().getCurrentEventName() + " event.");
				return false;
			}
			isEvent = true;
		}
		return true;
	}

	@Override
	protected Slot @Nullable [] get(Event event) {
		Block[] blocks;
		if (isEvent) {
			blocks = new Block[1];
			if (event instanceof BlockEvent blockEvent) {
				blocks[0] = blockEvent.getBlock();
			} else {
				return new Slot[0];
			}
		} else {
			assert this.blocks != null;
			blocks = this.blocks.getArray(event);
		}

		List<Slot> slots = new ArrayList<>();
		for (Block block : blocks) {
			BlockState state = block.getState();
			if (!(state instanceof Furnace))
				continue;
			FurnaceInventory furnaceInventory = ((Furnace) state).getInventory();
			if (isEvent && !Delay.isDelayed(event)) {
				slots.add(new FurnaceEventSlot(event, furnaceInventory));
			} else {
				slots.add(new InventorySlot(furnaceInventory, selectedSlot.ordinal()));
			}
		}
		return slots.toArray(new Slot[0]);
	}

	@Override
	public boolean isSingle() {
		if (isEvent)
			return true;
		assert blocks != null;
		return blocks.isSingle();
	}

	@Override
	public Class<? extends Slot> getReturnType() {
		return InventorySlot.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return selectedSlot.toString + " slot of " + (isEvent ? event.getEventName() : blocks.toString(event, debug));
	}

	@Override
	public boolean setTime(int time) {
		if (isEvent) { // getExpr will be null
			if (selectedSlot == FurnaceSlot.FUEL)
				return setTime(time, FurnaceBurnEvent.class);
			return setTime(time, FurnaceSmeltEvent.class);
		}
		return false;
	}

	private final class FurnaceEventSlot extends InventorySlot {

		private final Event event;

		public FurnaceEventSlot(Event event, FurnaceInventory furnaceInventory) {
			super(furnaceInventory, selectedSlot.ordinal());
			this.event = event;
		}

		@Override
		public @Nullable ItemStack getItem() {
			return switch (selectedSlot) {
				case INPUT -> {
					if (event instanceof FurnaceSmeltEvent furnaceSmeltEvent) {
						ItemStack source = furnaceSmeltEvent.getSource().clone();
						if (getTime() != EventValues.TIME_FUTURE)
							yield source;
						source.setAmount(source.getAmount() - 1);
						yield source;
					}
					yield super.getItem();
				}
				case FUEL -> {
					if (event instanceof FurnaceBurnEvent furnaceBurnEvent) {
						ItemStack fuel = furnaceBurnEvent.getFuel().clone();
						if (getTime() != EventValues.TIME_FUTURE)
							yield fuel;
						// a single lava bucket becomes an empty bucket
						// see https://minecraft.wiki/w/Smelting#Fuel
						// this is declared here because setting the amount to 0 may cause the ItemStack to become AIR
						Material newMaterial = fuel.getType() == Material.LAVA_BUCKET ? Material.BUCKET : Material.AIR;
						fuel.setAmount(fuel.getAmount() - 1);
						if (fuel.getAmount() == 0)
							fuel = new ItemStack(newMaterial);
						yield fuel;
					}
					yield super.getItem();
				}
				case OUTPUT -> {
					if (event instanceof FurnaceSmeltEvent furnaceSmeltEvent) {
						ItemStack result = furnaceSmeltEvent.getResult().clone();
						ItemStack currentResult = ((FurnaceInventory) getInventory()).getResult();
						if (currentResult != null)
							currentResult = currentResult.clone();
						if (getTime() != EventValues.TIME_FUTURE) { // 'past result slot' and 'result slot'
							yield currentResult;
						} else if (currentResult != null && currentResult.isSimilar(result)) { // 'future result slot'
							currentResult.setAmount(currentResult.getAmount() + result.getAmount());
							yield currentResult;
						} else {
							yield result; // 'the result'
						}
					}
					yield super.getItem();
				}
			};
		}

		@Override
		public void setItem(@Nullable ItemStack item) {
			if (selectedSlot == FurnaceSlot.OUTPUT && event instanceof FurnaceSmeltEvent furnaceSmeltEvent) {
				furnaceSmeltEvent.setResult(item != null ? item : new ItemStack(Material.AIR));
			} else {
				if (getTime() == EventValues.TIME_FUTURE) { // Since this is a future expression, run it AFTER the event
					Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.getInstance(), () -> FurnaceEventSlot.super.setItem(item));
				} else {
					super.setItem(item);
				}
			}
		}

	}

}
