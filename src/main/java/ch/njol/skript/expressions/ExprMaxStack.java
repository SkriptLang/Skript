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
package ch.njol.skript.expressions;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * @author joeuguce99
 */
@Name("Maximum Stack Size")
@Description("The maximum stack size of the specified material, e.g. 64 for torches, 16 for buckets, and 1 for swords.")
@Examples("send \"You can only pick up %max stack size of player's tool% of %type of (player's tool)%\" to player")
@Since("2.1")
public class ExprMaxStack extends SimplePropertyExpression<ItemType, Long> {

	static {
		register(ExprMaxStack.class, Long.class, "max[imum] stack[[ ]size]", "itemtype");
	}
	
	@SuppressWarnings("null")
	@Override
	public Long convert(ItemType itemType) {
		Object random = itemType.getRandomStackOrMaterial();
		if (random instanceof Material)
			return (long) ((Material) random).getMaxStackSize();
		return (long) ((ItemStack) random).getMaxStackSize();
	}

	@Override
	public Class<? extends Long> getReturnType() {
		return Long.class;
	}

	@Override
	protected String getPropertyName() {
		return "maximum stack size";
	}

}
