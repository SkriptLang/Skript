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

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.block.Block;
import org.bukkit.loot.Lootable;

@Name("Is A Lootable")
@Description("Checks whether an entity or block is a lootable. Lootables are entities or blocks that can have a loot table.")
@Examples("if entity is a lootable:")
@Since("INSERT VERSION")
public class CondIsALootable extends PropertyCondition<Object> {

	static {
		PropertyCondition.register(CondIsALootable.class, "lootable", "blocks/entities");
	}

	@Override
	public boolean check(Object object) {
		if (object instanceof Lootable)
			return true;
		if (object instanceof Block block)
			return block.getState() instanceof Lootable;
		return false;
	}

	@Override
	protected String getPropertyName() {
		return "lootable";
	}
}
