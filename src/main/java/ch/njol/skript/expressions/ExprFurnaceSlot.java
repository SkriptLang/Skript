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

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Furnace;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Getter;
import ch.njol.skript.util.slot.InventorySlot;
import ch.njol.skript.util.slot.Slot;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@Name("Furnace Slot")
@Description({"A slot of a furnace, i.e. either the ore, fuel or result slot.",
		"Remember to use '<a href='#ExprBlock'>block</a>' and not 'furnace', as 'furnace' is not an existing expression."})
@Examples({"set the fuel slot of the clicked block to a lava bucket",
		"set the block's ore slot to 64 iron ore",
		"give the result of the block to the player",
		"clear the result slot of the block"})
@Since("1.0")
@Events({"smelt", "fuel burn"})
public class ExprFurnaceSlot extends PropertyExpression<Block, Slot> {
	private final static int ORE = 0, FUEL = 1, RESULT = 2;
	private final static String[] slotNames = {"ore", "fuel", "result"};
	
	static {
		Skript.registerExpression(ExprFurnaceSlot.class, Slot.class, ExpressionType.PROPERTY,
				"(" + FUEL + "¦fuel|" + RESULT + "¦result) [slot]",
				"(" + ORE + "¦ore|" + FUEL + "¦fuel|" + RESULT + "¦result)[s] [slot[s]] of %blocks%",
				"%blocks%'[s] (" + ORE + "¦ore|" + FUEL + "¦fuel|" + RESULT + "¦result)[s] [slot[s]]");
	}
	
	int slot;
	boolean isEvent;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		isEvent = matchedPattern == 0;
		slot = parseResult.mark;
		if (isEvent && slot == RESULT && !getParser().isCurrentEvent(FurnaceSmeltEvent.class)) {
			Skript.error("Cannot use 'result slot' outside a fuel smelt event.");
			return false;
		} else if (isEvent && slot == FUEL && !getParser().isCurrentEvent(FurnaceBurnEvent.class)) {
			Skript.error("Cannot use 'fuel slot' outside a fuel burn event.");
			return false;
		}
		if (!isEvent)
			setExpr((Expression<Block>) exprs[0]);
		return true;
	}
	
	private final class FurnaceEventSlot extends InventorySlot {
		
		private final Event e;
		
		public FurnaceEventSlot(final Event e, final FurnaceInventory invi) {
			super(invi, slot);
			this.e = e;
		}
		
		@Override
		@Nullable
		public ItemStack getItem() {
			switch (slot) {
				case RESULT:
					if (e instanceof FurnaceSmeltEvent)
						return getTime() > -1 ? ((FurnaceSmeltEvent) e).getResult().clone() : super.getItem();
					else
						return super.getItem();
				case FUEL:
					if (e instanceof FurnaceBurnEvent)
						return getTime() > -1 ? ((FurnaceBurnEvent) e).getFuel().clone() : super.getItem();
					 else
						return pastItem();
				case ORE:
					if (e instanceof FurnaceSmeltEvent)
						return pastItem();
					else
						return super.getItem();
				default:
					return null;
			}
		}

		@SuppressWarnings("synthetic-access")
		@Override
		public void setItem(final @Nullable ItemStack item) {
			if (getTime() > -1) {
				Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.getInstance(),
						() -> FurnaceEventSlot.super.setItem(item));
			} else {
				if (e instanceof FurnaceSmeltEvent && slot == RESULT) {
					if (item != null)
						((FurnaceSmeltEvent) e).setResult(item);
					else
						((FurnaceSmeltEvent) e).setResult(new ItemStack(Material.AIR));
				} else {
					super.setItem(item);
				}
			}
		}

		@Nullable
		private ItemStack pastItem() {
			if (getTime() < 1) {
				return super.getItem();
			} else {
				ItemStack item = super.getItem();
				if (item == null)
					return null;
				item.setAmount(item.getAmount() - 1);
				return item.getAmount() == 0 ? new ItemStack(Material.AIR, 1) : item;
			}
		}
		
	}
	
	@Override
	protected Slot[] get(final Event e, final Block[] source) {
		return get(source, new Getter<Slot, Block>() {
			@Override
			@Nullable
			public Slot get(final Block b) {
				if (!ExprBurnCookTime.anyFurnace.isOfType(b))
					return null;
				if (isEvent && getTime() > -1 && !Delay.isDelayed(e)) {
					FurnaceInventory invi = ((Furnace) b.getState()).getInventory();
					return new FurnaceEventSlot(e, invi);
				} else {
					FurnaceInventory invi = ((Furnace) b.getState()).getInventory();
					return new InventorySlot(invi, slot);
				}
			}
		});
	}
	
	@Override
	public Class<Slot> getReturnType() {
		return Slot.class;
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		if (e == null)
			return "the " + (getTime() == -1 ? "past " : getTime() == 1 ? "future " : "") + slotNames[slot] + " slot of " + getExpr().toString(e, debug);
		return Classes.getDebugMessage(getSingle(e));
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean setTime(final int time) {
		return super.setTime(time, getExpr(), FurnaceSmeltEvent.class, FurnaceBurnEvent.class);
	}
	
}
