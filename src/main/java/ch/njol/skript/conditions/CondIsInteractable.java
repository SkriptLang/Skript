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
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.conditions;

import org.bukkit.Material;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;

@Name("Is Interactable")
@Description("Checks wether or not a block is interactable.")
@Examples({"on block break:",
			"\tif event-block is interactable:",
			"\t\tcancel event",
			"\t\tsend \"You cannot break interactable blocks!\""})
@Since("2.5.2")
@RequiredPlugins("Minecraft 1.13+")
public class CondIsInteractable extends PropertyCondition<ItemType> {
	
	static {
		if (Skript.methodExists(Material.class, "isInteractable")) {
			register(CondIsInteractable.class, "interactable", "itemtypes");
		}
	}
	
	@Override
	public boolean check(ItemType item) {
		return item.getMaterial().isInteractable();
	}
	
	@Override
	protected String getPropertyName() {
		return "interactable";
	}
}
