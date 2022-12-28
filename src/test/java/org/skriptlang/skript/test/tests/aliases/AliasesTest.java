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
package org.skriptlang.skript.test.tests.aliases;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.junit.Test;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.registrations.Classes;

public class AliasesTest {

	@Test
	public void test() {
		ItemStack itemstack = new ItemStack(Material.LEATHER_CHESTPLATE, 6);
		ItemMeta meta = itemstack.getItemMeta();
		assert meta instanceof LeatherArmorMeta;
		LeatherArmorMeta leather = (LeatherArmorMeta) meta;
		leather.setColor(Color.LIME);
		itemstack.setItemMeta(leather);
		ItemType itemType = new ItemType(itemstack);
		assert itemType.equals(new ItemType(itemstack));

		itemstack = new ItemStack(Material.LEATHER_CHESTPLATE, 2);
		meta = itemstack.getItemMeta();
		assert meta instanceof LeatherArmorMeta;
		leather = (LeatherArmorMeta) meta;
		leather.setColor(Color.RED);
		itemstack.setItemMeta(leather);
		assert !itemType.equals(new ItemType(itemstack));

		// Contains assert inside serialize method too, Njol mentioned this.
		assert Classes.serialize(itemType) != null;
		// This doesn't work anymore since Njol added this.
		//assert Classes.serialize(itemType).equals(Classes.serialize(itemType));
		assert !Classes.serialize(itemType).equals(Classes.serialize(new ItemType(itemstack)));
	}

}
