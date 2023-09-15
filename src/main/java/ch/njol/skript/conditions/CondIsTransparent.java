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
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.conditions;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;

@Name("Is Transparent")
@Description("Checks whether an item is transparent. Note that this condition may not work for all blocks, due to the transparency list used by Spigot not being completely accurate.")
@Examples("player's tool is transparent.")
@Since("2.2-dev36")
public class CondIsTransparent extends PropertyCondition<ItemType> {

	static {
		register(CondIsTransparent.class, "transparent", "itemtypes");
	}

	@Override
	public boolean check(ItemType i) {
		return i.getMaterial().isTransparent();
	}

	@Override
	protected String getPropertyName() {
		return "transparent";
	}

}
