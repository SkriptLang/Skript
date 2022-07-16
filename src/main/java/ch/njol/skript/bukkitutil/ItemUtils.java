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
package ch.njol.skript.bukkitutil;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;

/**
 * Miscellaneous static utility methods related to items.
 */
public class ItemUtils {

	/**
	 * Gets damage/durability of an item, or 0 if it does not have damage.
	 * @param stack Item.
	 * @return Damage.
	 */
	public static int getDamage(ItemStack stack) {
		ItemMeta meta = stack.getItemMeta();
		if (meta instanceof Damageable)
			return ((Damageable) meta).getDamage();
		return 0; // Not damageable item
	}
	
	/**
	 * Sets damage/durability of an item if possible.
	 * @param stack Item to modify.
	 * @param damage New damage. Note that on some Minecraft versions,
	 * this might be truncated to short.
	 */
	public static void setDamage(ItemStack stack, int damage) {
		ItemMeta meta = stack.getItemMeta();
		if (meta instanceof Damageable) {
			((Damageable) meta).setDamage(damage);
			stack.setItemMeta(meta);
		}
	}

	/**
	 * Gets a block material corresponding to given item material, which might
	 * be the given material. If no block material is found, null is returned.
	 * @param type Material.
	 * @return Block version of material or null.
	 */
	@Nullable
	public static Material asBlock(Material type) {
		if (type.isBlock()) {
			return type;
		} else {
			return null;
		}
	}
	
	/**
	 * Gets an item material corresponding to given block material, which might
	 * be the given material.
	 * @param type Material.
	 * @return Item version of material or null.
	 */
	public static Material asItem(Material type) {
		// Assume (naively) that all types are valid items
		return type;
	}
	
	/**
	 * Tests whether two item stacks are of the same type, i.e. it ignores the amounts.
	 *
	 * @param is1
	 * @param is2
	 * @return Whether the item stacks are of the same type
	 */
	public static boolean itemStacksEqual(final @Nullable ItemStack is1, final @Nullable ItemStack is2) {
		if (is1 == null || is2 == null)
			return is1 == is2;
		return is1.getType() == is2.getType() && ItemUtils.getDamage(is1) == ItemUtils.getDamage(is2)
			&& is1.getItemMeta().equals(is2.getItemMeta());
	}
	
}
