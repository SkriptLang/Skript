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

import org.bukkit.entity.Entity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import ch.njol.skript.bukkitutil.ItemUtils;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.util.slot.Slot;

@Name("Is Empty")
@Description({
	"Checks whether an inventory, an inventory slot, or a text is empty.",
	"An entity can be a type, which will check if there are no passengers."
})
@Examples("player's inventory is empty")
@Since("<i>unknown</i> (before 2.1), INSERT VERSION (Entity)")
public class CondIsEmpty extends PropertyCondition<Object> {

	static {
		register(CondIsEmpty.class, "empty", "entities/inventories/slots/strings");
	}

	@Override
	public boolean check(Object object) {
		if (object instanceof String)
			return ((String) object).isEmpty();
		if (object instanceof Inventory) {
			for (ItemStack item : ((Inventory) object).getContents()) {
				if (item != null && !ItemUtils.isAir(item.getType()))
					return false; // There is an item here!
			}
			return true;
		}
		if (object instanceof Slot) {
			Slot slot = (Slot) object;
			ItemStack item = slot.getItem();
			return item == null || !ItemUtils.isAir(item.getType());
		}
		if (object instanceof Entity) {
			return ((Entity) object).isEmpty();
		}
		assert false;
		return false;
	}

	@Override
	protected String getPropertyName() {
		return "empty";
	}

}
