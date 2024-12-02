/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
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
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Name("Furnace Slot")
@Description({
	"A slot of a furnace, i.e. either the ore, fuel or result slot.",
	"Remember to use '<a href='#ExprBlock'>block</a>' and not <code>furnace</code>, as <code>furnace</code> is not an existing expression.",
	"Note that <code>the result</code> and <code>the result slot</code> refer to separate things. <code>the result</code> is the product in a smelt event " +
	"and <code>the result slot</code> is the output slot of a furnace (where <code>the result</code> will end up).",
	"Note that if the result in a smelt event is changed to an item that differs in type from the items currently in " +
	"the result slot, the smelting will fail to complete (the item will attempt to smelt itself again).",
	"Note that if values other than <code>the result</code> are changed, event values may not accurately reflect the actual items in a furnace.",
	"Thus you may wish to use the event block in this case (e.g. <code>the fuel slot of the event-block</code>) to get accurate values if needed."
})
@Examples({
	"set the fuel slot of the clicked block to a lava bucket",
	"set the block's ore slot to 64 iron ore",
	"give the result of the block to the player",
	"clear the result slot of the block"
})
@Events({"smelt", "fuel burn"})
@Since("1.0, 2.8.0 (syntax rework)")
public class ExprFurnaceSlot extends SimpleExpression<Slot> {

	private static final int ORE = 0, FUEL = 1, RESULT = 2;
	
	static {
		Skript.registerExpression(ExprFurnaceSlot.class, Slot.class, ExpressionType.PROPERTY,
				"[the] (0:ore slot|1:fuel slot|2:result [5:slot])",
				"[the] (0:ore|1:fuel|2:result) slot[s] of %blocks%",
				"%blocks%'[s] (0:ore|1:fuel|2:result) slot[s]"
		);
	}

	@Nullable
	private Expression<Block> blocks;
	private boolean isEvent;
	private boolean isResultSlot;
	private int slot;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		isEvent = matchedPattern == 0;
		if (!isEvent)
			blocks = (Expression<Block>) exprs[0];

		slot = parseResult.mark;
		isResultSlot = slot == 7;
		if (isResultSlot)
			slot = RESULT;

		if (isEvent && (slot == ORE || slot == RESULT) && !getParser().isCurrentEvent(FurnaceSmeltEvent.class)) {
			Skript.error("Cannot use 'result slot' or 'ore slot' outside an ore smelt event.");
			return false;
		} else if (isEvent && slot == FUEL && !getParser().isCurrentEvent(FurnaceBurnEvent.class)) {
			Skript.error("Cannot use 'fuel slot' outside a fuel burn event.");
			return false;
		}

		return true;
	}

	@Override
	@Nullable
	protected Slot[] get(Event event) {
		Block[] blocks;
		if (isEvent) {
			blocks = new Block[1];
			if (event instanceof FurnaceSmeltEvent) {
				blocks[0] = ((FurnaceSmeltEvent) event).getBlock();
			} else if (event instanceof FurnaceBurnEvent) {
				blocks[0] = ((FurnaceBurnEvent) event).getBlock();
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
			} else { // Normal inventory slot is fine since the time will always be in the present
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
		String time = (getTime() == -1) ? "past " : (getTime() == 1) ? "future " : "";
		String slotName = (slot == ORE) ? "ore" : (slot == FUEL) ? "fuel" : "result";
		if (isEvent) {
			return "the " + time + slotName + (isResultSlot ? " slot" : "");
		} else {
			assert blocks != null;
			return "the " + time + slotName + " slot of " + blocks.toString(event, debug);
		}
	}

	@Override
	public boolean setTime(int time) {
		if (isEvent) { // getExpr will be null
			if (slot == RESULT && !isResultSlot) { // 'the past/future result' - doesn't make sense, don't allow it
				return false;
			} else if (slot == FUEL) {
				return setTime(time, FurnaceBurnEvent.class);
			} else {
				return setTime(time, FurnaceSmeltEvent.class);
			}
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
			switch (slot) {
				case ORE:
					if (event instanceof FurnaceSmeltEvent) {
						ItemStack source = ((FurnaceSmeltEvent) event).getSource().clone();
						if (getTime() != EventValues.TIME_FUTURE)
							return source;
						source.setAmount(source.getAmount() - 1);
						return source;
					}
					return super.getItem();
				case FUEL:
					if (event instanceof FurnaceBurnEvent) {
						ItemStack fuel = ((FurnaceBurnEvent) event).getFuel().clone();
						if (getTime() != EventValues.TIME_FUTURE)
							return fuel;
						// a single lava bucket becomes an empty bucket
						// see https://minecraft.wiki/w/Smelting#Fuel
						// this is declared here because setting the amount to 0 may cause the ItemStack to become AIR
						Material newMaterial = fuel.getType() == Material.LAVA_BUCKET ? Material.BUCKET : Material.AIR;
						fuel.setAmount(fuel.getAmount() - 1);
						if (fuel.getAmount() == 0)
							fuel = new ItemStack(newMaterial);
						return fuel;
					}
					return super.getItem();
				case RESULT:
					if (event instanceof FurnaceSmeltEvent) {
						ItemStack result = ((FurnaceSmeltEvent) event).getResult().clone();
						if (isResultSlot) { // Special handling for getting the result slot
							ItemStack currentResult = ((FurnaceInventory) getInventory()).getResult();
							if (currentResult != null)
								currentResult = currentResult.clone();
							if (getTime() != EventValues.TIME_FUTURE) { // 'past result slot' and 'result slot'
								return currentResult;
							} else if (currentResult != null && currentResult.isSimilar(result)) { // 'future result slot'
								currentResult.setAmount(currentResult.getAmount() + result.getAmount());
								return currentResult;
							} else {
								return result;
							}
						}
						// 'the result'
						return result;
					}
					return super.getItem();
			}
			return null;
		}

		@Override
		public void setItem(@Nullable ItemStack item) {
			if (slot == RESULT && !isResultSlot && event instanceof FurnaceSmeltEvent) {
				((FurnaceSmeltEvent) event).setResult(item != null ? item : new ItemStack(Material.AIR));
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
