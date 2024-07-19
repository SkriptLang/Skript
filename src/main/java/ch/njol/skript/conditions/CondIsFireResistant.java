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

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import org.bukkit.inventory.meta.ItemMeta;

@Name("Is Fire Resistant")
@Description("Checks whether an item is fire resistant.")
@Examples({
	"if player's tool is fire resistant:",
	"if {_items::*} aren't resistant to fire:"
})
@RequiredPlugins("Spigot 1.20.5+")
@Since("2.9.0")
public class CondIsFireResistant extends PropertyCondition<ItemType> {

	static {
		if (Skript.methodExists(ItemMeta.class, "isFireResistant"))
			PropertyCondition.register(CondIsFireResistant.class, "(fire resistant|resistant to fire)", "itemtypes");
	}

	@Override
	public boolean check(ItemType item) {
		return item.getItemMeta().isFireResistant();
	}

	@Override
	public String getPropertyName() {
		return "fire resistant";
	}

}
