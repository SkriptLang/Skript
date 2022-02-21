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
import ch.njol.skript.doc.RequiredPlugins;
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
@Description({"Returns the experience cost (in levels) to complete the current repair or the maximum experience cost (in levels) to be allowed by the current repair.",
			  "The default value of max cost set by vanilla Minecraft is 40."})
@Examples({
		"on inventory click:",
		"\tif {AnvilRepairSaleActive} = true:",
		"\t\twait a tick # recommended, to avoid client bugs",
		"\t\tset anvil repair cost to anvil repair cost * 50%",
		"\t\tsend \"Anvil repair sale is ON!\" to player",

		"on inventory click:",
		"\tplayer have permission \"anvil.repair.max.bypass\"",
		"\tset max repair cost of event-inventory to 99999"})
@Since("INSERT VERSION")
@RequiredPlugins("MC 1.11, MC 1.13+ (max cost)")
public class ExprAnvilRepairCost extends SimplePropertyExpression<Inventory, Integer> {

	static {
		String maxCostSyntax = Skript.methodExists(AnvilInventory.class, "getMaximumRepairCost") ? " [:max[imum]]" : "";

		if (Skript.methodExists(AnvilInventory.class, "getRepairCost"))
			Skript.registerExpression(ExprAnvilRepairCost.class, Integer.class, ExpressionType.PROPERTY,
				"[the] [anvil] [item]" + maxCostSyntax + " repair cost [of %inventories%]",
				"%inventories%'[s] [item]" + maxCostSyntax + " repair cost");
	}

	boolean isMax = false;
	Expression<Inventory> invs;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		invs = (Expression<Inventory>) exprs[0];
		isMax = parseResult.hasTag("max");
		setExpr((Expression<? extends Inventory>) exprs[0]);
		return true;
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
		assert delta != null;
		if (delta == null || delta[0] == null)
			return;

		for (Inventory inv : invs.getArray(e)) {
			if (inv instanceof AnvilInventory) {
				AnvilInventory aInv = (AnvilInventory) inv;
				int value = ((Number) delta[0]).intValue();
				int change = mode == ChangeMode.SET ? 0 : (isMax ? aInv.getMaximumRepairCost() : aInv.getRepairCost());
				int newValue = Math.max((change + value), 0);

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
		return "anvil item" + (isMax ? " max" : "") + " repair cost";
	}
	
}
