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
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Name("Crafting Inventory Slots")
@Description({"Represents the slots/items in the crafting inventory such as the result item and matrix of the items."})
@Examples({"on craft:",
	"\tif crafting result item is paper:",
	"\t\tset the crafting matrix to air, air, air, paper, diamond, paper, air, air and air",
	"on preparing crafting:",
	"\tset crafting result item to wood"})
@Since("INSERT VERSION")
public class ExprCraftingSlots extends SimpleExpression<ItemStack> {
	
	static {
		Skript.registerExpression(ExprCraftingSlots.class, ItemStack.class, ExpressionType.COMBINED,
				"[the] crafting [inventory] result (slot|item) [of %inventories%]",
				"%inventories%'[s] crafting [inventory] result (slot|item)",
				"[the] crafting [inventory] (matrix|grid) [(slots|items|shape)] [of %inventories%]",
				"%inventories%'[s] crafting [inventory] (matrix|grid) [(slots|items|shape)]");
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<Inventory> invis;
	private boolean isMatrix;
	final ItemStack AIR = new ItemStack(Material.AIR);
	final ItemStack[] AIR_ITEMSTACK_LIST = new ItemStack[]{AIR, AIR, AIR, AIR, AIR, AIR, AIR, AIR, AIR};

	@Override
	@SuppressWarnings({"null", "unchecked"})
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		invis = (Expression<Inventory>) exprs[0];
		isMatrix = matchedPattern > 1;
		return true;
	}

	@Override
	@Nullable
	protected ItemStack[] get(Event e) {
		Inventory[] invis = this.invis.getArray(e);
		if (invis == null)
			return null;

		List<ItemStack> items = new ArrayList<>();
		for (Inventory invi : invis) {
			if (!(invi instanceof CraftingInventory))
				continue;

			CraftingInventory craft = (CraftingInventory) invi;
			if (isMatrix) {
				items.addAll(Arrays.asList(craft.getMatrix()));
			} else {
				items.add(craft.getResult());
			}
		}
		return items.toArray(new ItemStack[0]);
	}

	@Override
	public @Nullable Class<?>[] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.DELETE)
			return CollectionUtils.array(ItemStack[].class);

		return null;
		}

	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		Inventory[] invis = this.invis.getArray(e);
		ItemStack[] items = (ItemStack[]) delta;
		if (invis == null)
			return;

		if (e instanceof PrepareItemCraftEvent && isMatrix) { // Until spigot/paper fixes this
			Skript.warning("Editing the crafting inventory matrix in player " +
				"preparing craft event will result in an infinite loop due " +
				"to how spigot/paper handle this event, therefore it's disabled for now.");
			return;
		}

		if (mode == ChangeMode.DELETE) {
			for (Inventory invi : invis) {
				if (!(invi instanceof CraftingInventory))
					continue;
				if (isMatrix)
					((CraftingInventory) invi).setMatrix(AIR_ITEMSTACK_LIST);
				else
					((CraftingInventory) invi).setResult(new ItemStack(Material.AIR));
			}
		} else { // SET
			assert invis != null;
			if (items == null)
				return;
			for (Inventory invi : invis) {
				if (!(invi instanceof CraftingInventory))
					continue;
				if (isMatrix) {
					ItemStack[] itemStacks = new ItemStack[9];
					for (int i = 0; i < 9; i++) { // list must be 9 if not we will fill it manually with AIR
						itemStacks[i] = items.length >= (i+1) ? items[i] : AIR;
					}
					((CraftingInventory) invi).setMatrix(itemStacks);
				} else {
					if (items[0] != null) {
						((CraftingInventory) invi).setResult(items[0]);
					}
				}
			}
		}
	}

	@Override
	public boolean isSingle() {
		return invis.isSingle() && !isMatrix;
	}

	@Override
	public Class<? extends ItemStack> getReturnType() {
		return ItemStack.class;
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "crafting " + (isMatrix ? "matrix" : "result") + " slot of " + invis.toString(e, debug);
	}
}
