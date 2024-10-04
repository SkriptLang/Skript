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

	private static final int ORE = 0, FUEL = 1, RESULT = 2;

	static {
		Skript.registerExpression(ExprFurnaceSlot.class, Slot.class, ExpressionType.PROPERTY,
			"[the] (0:ore|input|1:fuel|2:result|output) slot[s] [of %blocks%]",
			"%blocks%['s] (0:ore|input|1:fuel|2:result|output) slot[s]"
		);
	}


	private @Nullable Expression<Block> blocks;
	private boolean isEvent;
	private int slot;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {

		if (exprs[0] != null) {
			blocks = (Expression<Block>) exprs[0];
		} else {
			if (!getParser().isCurrentEvent(FurnaceBurnEvent.class, FurnaceStartSmeltEvent.class, FurnaceExtractEvent.class, FurnaceSmeltEvent.class)) {
				Skript.error("There's no furnace in a " + getParser().getCurrentEventName() + " event.");
				return false;
			}
			isEvent = true;
		}
		slot = parseResult.mark;
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
				slots.add(new InventorySlot(furnaceInventory, slot));
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
		String result = "";
		switch (slot) {
			case ORE -> result = "input slot";
			case FUEL -> result = "fuel slot";
			case RESULT -> result = "result slot";
		}
		if (isEvent) {
			result += " of " + event.getEventName();
		} else {
			result += " of " + blocks.toString(event, debug);
		}
		return result;
	}

	@Override
	public boolean setTime(int time) {
		if (isEvent) { // getExpr will be null
			if (slot == FUEL)
				return setTime(time, FurnaceBurnEvent.class);
			return setTime(time, FurnaceSmeltEvent.class);
		}
		return false;
	}

	private final class FurnaceEventSlot extends InventorySlot {

		private final Event event;

		public FurnaceEventSlot(Event event, FurnaceInventory furnaceInventory) {
			super(furnaceInventory, slot);
			this.event = event;
		}

		@Override
		@Nullable
		public ItemStack getItem() {
            return switch (slot) {
                case ORE -> {
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
                        Material newMaterial = fuel.getType() == Material.LAVA_BUCKET ? Material.BUCKET : Material.AIR;
                        fuel.setAmount(fuel.getAmount() - 1);
                        if (fuel.getAmount() == 0)
                            fuel = new ItemStack(newMaterial);
                        yield fuel;
                    }
                    yield super.getItem();
                    // a single lava bucket becomes an empty bucket
                    // see https://minecraft.wiki/w/Smelting#Fuel
                    // this is declared here because setting the amount to 0 may cause the ItemStack to become AIR
                }
                case RESULT -> {
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
                default -> null;
            };
        }

		@Override
		public void setItem(@Nullable ItemStack item) {
			if (slot == RESULT && event instanceof FurnaceSmeltEvent furnaceSmeltEvent) {
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
