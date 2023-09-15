/**
 * This file is part of Skript.
 * <p>
 * Skript is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Skript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * <p>
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.*;
import org.bukkit.Material;

@Name("Is Fuel")
@Description("Checks whether an item can be used as fuel in a furnace.")
@Examples({"on right click on furnace:",
	"\tif player's tool is not fuel:",
	"\t\tsend \"Please hold a valid fuel item in your hand\"",
	"\t\tcancel event"
})
@Since("2.5.1")
@RequiredPlugins("Minecraft 1.11.2+")
public class CondIsFuel extends PropertyCondition<ItemType> {

	static {
		if (Skript.methodExists(Material.class, "isFuel")) {
			register(CondIsFuel.class, "[furnace] fuel", "itemtypes");
		}
	}

	@Override
	public boolean check(ItemType item) {
		return item.getMaterial().isFuel();
	}

	@Override
	protected String getPropertyName() {
		return "fuel";
	}

}
