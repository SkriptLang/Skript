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
