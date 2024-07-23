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

import org.bukkit.inventory.ItemStack;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;

@Name("Is Stackable")
@Description("Checks whether an item is stackable.")
@Examples({
	"diamond axe is stackable",
	"birch wood is stackable",
	"torch is stackable"
})
@Since("2.7")
public class CondIsStackable extends PropertyCondition<ItemStack> {

	static {
		register(CondIsStackable.class, "stackable", "itemstacks");
	}

	@Override
	public boolean check(ItemStack item) {
		return item.getMaxStackSize() > 1;
	}

	@Override
	protected String getPropertyName() {
		return "stackable";
	}

}
