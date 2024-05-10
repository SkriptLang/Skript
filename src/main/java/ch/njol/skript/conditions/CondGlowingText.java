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
import org.bukkit.block.Block;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;

@Name("Has Glowing Text")
@Description("Checks whether a sign (either a block or an item) has glowing text")
@Examples("if target block has glowing text")
@Since("2.8.0")
public class CondGlowingText extends PropertyCondition<Object> {

	static {
		if (Skript.methodExists(Sign.class, "isGlowingText"))
			register(CondGlowingText.class, PropertyType.HAVE, "glowing text", "blocks/itemtypes");
	}

	@Override
	public boolean check(Object obj) {
		if (obj instanceof Block) {
			BlockState state = ((Block) obj).getState();
			return state instanceof Sign && ((Sign) state).isGlowingText();
		} else if (obj instanceof ItemType) {
			ItemMeta meta = ((ItemType) obj).getItemMeta();
			if (meta instanceof BlockStateMeta) {
				BlockState state = ((BlockStateMeta) meta).getBlockState();
				return state instanceof Sign && ((Sign) state).isGlowingText();
			}
		}
		return false;
	}

	@Override
	protected PropertyType getPropertyType() {
		return PropertyType.HAVE;
	}

	@Override
	protected String getPropertyName() {
		return "glowing text";
	}

}
