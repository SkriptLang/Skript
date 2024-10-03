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
package ch.njol.skript.conditions;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.util.slot.Slot;
import org.skriptlang.skript.lang.util.SkriptQueue;

/**
 * @author Peter Güttinger
 */
@Name("Is Empty")
@Description("Checks whether an inventory, an inventory slot, a queue, or a text is empty.")
@Examples("player's inventory is empty")
@Since("<i>unknown</i> (before 2.1)")
public class CondIsEmpty extends PropertyCondition<Object> {

	static {
		register(CondIsEmpty.class, "empty", "queues/inventories/slots/strings");
	}

	@Override
	public boolean check(final Object o) {
		if (o instanceof String)
			return ((String) o).isEmpty();
		if (o instanceof SkriptQueue queue)
			return queue.isEmpty();
		if (o instanceof Inventory) {
			for (ItemStack s : ((Inventory) o).getContents()) {
				if (s != null && s.getType() != Material.AIR)
					return false; // There is an item here!
			}
			return true;
		}
		if (o instanceof Slot) {
			final Slot s = (Slot) o;
			final ItemStack i = s.getItem();
			return i == null || i.getType() == Material.AIR;
		}
		assert false;
		return false;
	}

	@Override
	protected String getPropertyName() {
		return "empty";
	}

}
