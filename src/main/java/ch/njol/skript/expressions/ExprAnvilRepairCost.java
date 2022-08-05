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
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.eclipse.jdt.annotation.Nullable;

@Name("Anvil Repair Cost")
@Description({
	"An expression to get the number of levels required to complete the current repair or the maximum allowed number of levels the current repair can cost.",
	"The default maximum allowed repair cost is 40 levels."
})
@Examples({
	"on inventory click:",
	"\tif {AnvilRepairSaleActive} = true:",
	"\t\twait a tick # recommended, to avoid client bugs",
	"\t\tset {_currentCost} to the anvil item repair cost of the event-inventory",
	"\t\tset the anvil item repair cost of the event-inventory to {_currentCost} * 50%",
	"\t\tsend \"The Anvil Repair Sale is active!\" to player",

	"on inventory click:",
	"\ttype of event-inventory is anvil inventory",
	"\tthe player has the permission \"anvil.repair.max.bypass\"",
	"\tset the max repair cost of the event-inventory to 99999"
})
@Since("INSERT VERSION")
public class ExprAnvilRepairCost extends SimplePropertyExpression<Inventory, Integer> {

	static {
		Skript.registerExpression(ExprAnvilRepairCost.class, Integer.class, ExpressionType.PROPERTY,
			"[the] [anvil] [item] [:max[imum]] repair cost [of %inventories%]",
			"%inventories%'[s] [item] [:max[imum]] repair cost");
	}

	private boolean isMax = false;
	private static final int DEFAULT_MAX_VALUE = 40;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		isMax = parseResult.hasTag("max");
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	@Nullable
	public Integer convert(Inventory inv) {
		if (!(inv instanceof AnvilInventory))
			return null;
		AnvilInventory aInv = (AnvilInventory) inv;
		return isMax ? aInv.getMaximumRepairCost() : aInv.getRepairCost();
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		switch (mode) {
			case RESET:
				if (!isMax) {
					Skript.error("Repair cost cannot be reset");
					return null;
				}
			case ADD:
			case REMOVE:
			case SET:
				return CollectionUtils.array(Number.class);
			default:
				return null;
		}
	}

	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		if (mode != ChangeMode.RESET && delta == null)
			return;

		int value = (mode == ChangeMode.RESET && isMax) ? DEFAULT_MAX_VALUE : ((Number) delta[0]).intValue() * (mode == ChangeMode.REMOVE ? -1 : 1);
		for (Inventory inv : getExpr().getArray(e)) {
			if (inv instanceof AnvilInventory) {
				AnvilInventory aInv = (AnvilInventory) inv;
				int originalValue = (mode == ChangeMode.SET || mode == ChangeMode.RESET) ? 0 : (isMax ? aInv.getMaximumRepairCost() : aInv.getRepairCost());
				int newValue = Math.max((originalValue + value), 0);

				if (isMax)
					aInv.setMaximumRepairCost(newValue);
				else
					aInv.setRepairCost(newValue);
			}
		}
	}

	@Override
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	public String getPropertyName() {
		return "anvil item" + (isMax ? " maximum" : "") + " repair cost";
	}
	
}
