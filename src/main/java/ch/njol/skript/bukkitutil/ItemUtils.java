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
import org.bukkit.TreeType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;

import java.util.HashMap;

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

	// Only 1.15 and versions after have Material#isAir method
	private static final boolean IS_AIR_EXISTS = Skript.methodExists(Material.class, "isAir");

	public static boolean isAir(Material type) {
		if (IS_AIR_EXISTS)
			return type.isAir();
		return type == Material.AIR || type == Material.CAVE_AIR || type == Material.VOID_AIR;
	}

	// TreeType -> Sapling (Material) conversion for EvtGrow
	private static final HashMap<TreeType, Material> TREE_TO_SAPLING_MAP = new HashMap<>();
	static {
		// Populate TREE_TO_SAPLING_MAP
		// oak
		TREE_TO_SAPLING_MAP.put(TreeType.TREE, Material.OAK_SAPLING);
		TREE_TO_SAPLING_MAP.put(TreeType.BIG_TREE, Material.OAK_SAPLING);
		TREE_TO_SAPLING_MAP.put(TreeType.SWAMP, Material.OAK_SAPLING);
		// spruce
		TREE_TO_SAPLING_MAP.put(TreeType.REDWOOD, Material.SPRUCE_SAPLING);
		TREE_TO_SAPLING_MAP.put(TreeType.TALL_REDWOOD, Material.SPRUCE_SAPLING);
		TREE_TO_SAPLING_MAP.put(TreeType.MEGA_REDWOOD, Material.SPRUCE_SAPLING);
		// birch
		TREE_TO_SAPLING_MAP.put(TreeType.BIRCH, Material.BIRCH_SAPLING);
		TREE_TO_SAPLING_MAP.put(TreeType.TALL_BIRCH, Material.BIRCH_SAPLING);
		// jungle
		TREE_TO_SAPLING_MAP.put(TreeType.JUNGLE, Material.JUNGLE_SAPLING);
		TREE_TO_SAPLING_MAP.put(TreeType.SMALL_JUNGLE, Material.JUNGLE_SAPLING);
		TREE_TO_SAPLING_MAP.put(TreeType.JUNGLE_BUSH, Material.JUNGLE_SAPLING);
		TREE_TO_SAPLING_MAP.put(TreeType.COCOA_TREE, Material.JUNGLE_SAPLING);
		// acacia
		TREE_TO_SAPLING_MAP.put(TreeType.ACACIA, Material.ACACIA_SAPLING);
		// dark oak
		TREE_TO_SAPLING_MAP.put(TreeType.DARK_OAK, Material.DARK_OAK_SAPLING);

		// mushrooms
		TREE_TO_SAPLING_MAP.put(TreeType.BROWN_MUSHROOM, Material.BROWN_MUSHROOM);
		TREE_TO_SAPLING_MAP.put(TreeType.RED_MUSHROOM, Material.RED_MUSHROOM);

		// chorus
		TREE_TO_SAPLING_MAP.put(TreeType.CHORUS_PLANT, Material.CHORUS_FLOWER);

		// nether
		if (Skript.isRunningMinecraft(1, 16)) {
			TREE_TO_SAPLING_MAP.put(TreeType.WARPED_FUNGUS, Material.WARPED_FUNGUS);
			TREE_TO_SAPLING_MAP.put(TreeType.CRIMSON_FUNGUS, Material.CRIMSON_FUNGUS);
		}

		// azalea
		if (Skript.isRunningMinecraft(1, 17))
			TREE_TO_SAPLING_MAP.put(TreeType.AZALEA, Material.AZALEA);

		// mangrove
		if (Skript.isRunningMinecraft(1, 19)) {
			TREE_TO_SAPLING_MAP.put(TreeType.MANGROVE, Material.MANGROVE_PROPAGULE);
			TREE_TO_SAPLING_MAP.put(TreeType.TALL_MANGROVE, Material.MANGROVE_PROPAGULE);
		}

		// cherry
		if (Skript.isRunningMinecraft(1, 19, 4))
			TREE_TO_SAPLING_MAP.put(TreeType.CHERRY, Material.CHERRY_SAPLING);
	}

	public static Material getTreeSapling(TreeType treeType) {
		return TREE_TO_SAPLING_MAP.get(treeType);
	}
	
}
