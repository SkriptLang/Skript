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

import ch.njol.skript.aliases.ItemData;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;

@Name("Is Plain")
@Description({
	"Checks if an itemtype is plain.",
	"This is the simplest material item the provided itemtype represents.",
	"Example being an enchanted apple with a name is not plain.",
	"Whereas something like dirt is plain with no properties."
})
@Examples("if an apple is plain")
@Since("INSERT VERSION")
public class CondIsPlain extends PropertyCondition<ItemType> {

	static {
		register(CondIsPlain.class, "plain", "itemtypes");
	}

	@Override
	public boolean check(ItemType itemtype) {
		return itemtype.getTypes().stream().allMatch(ItemData::isPlain);
	}

	@Override
	protected String getPropertyName() {
		return "plain";
	}

}
