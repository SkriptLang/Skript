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

import org.bukkit.event.inventory.InventoryAction;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;

@Name("Inventory Action")
@Description("The <a href='./classes.html#inventoryaction'>inventory action</a> of an inventory event. Please click on the link for more information.")
@Examples("inventory action is pickup all")
@Since("2.2-dev16")
public class ExprInventoryAction extends EventValueExpression<InventoryAction> {

	static {
		register(ExprInventoryAction.class, InventoryAction.class, "inventory action");
	}

	public ExprInventoryAction() {
		super(InventoryAction.class);
	}

}
