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

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.util.slot.Slot;
import com.destroystokyo.paper.profile.PlayerProfile;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Tag;
import org.bukkit.TreeType;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Fence;
import org.bukkit.block.data.type.Gate;
import org.bukkit.block.data.type.Wall;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.UUID;

/**
 * Miscellaneous static utility methods related to items.
 */
public class ItemUtils {

	public static final boolean HAS_MAX_DAMAGE = Skript.methodExists(Damageable.class, "getMaxDamage");
	// Introduced in Paper 1.21
	public static final boolean HAS_RESET = Skript.methodExists(Damageable.class, "resetDamage");
	public static final boolean CAN_CREATE_PLAYER_PROFILE = Skript.methodExists(Bukkit.class, "createPlayerProfile", UUID.class, String.class);
	// paper does not do texture lookups by default
	public static final boolean REQUIRES_TEXTURE_LOOKUP = Skript.classExists("com.destroystokyo.paper.profile.PlayerProfile") && Skript.isRunningMinecraft(1, 19, 4);

	/**
	 * Gets damage/durability of an item, or 0 if it does not have damage.
	 * @param itemStack Item.
	 * @return Damage.
	 */
	public static int getDamage(ItemStack itemStack) {
		return getDamage(itemStack.getItemMeta());
	}

	/**
	 * Gets damage/durability of an itemmeta, or 0 if it does not have damage.
	 * @param itemMeta ItemMeta.
	 * @return Damage.
	 */
	public static int getDamage(ItemMeta itemMeta) {
		if (itemMeta instanceof Damageable)
			return ((Damageable) itemMeta).getDamage();
		return 0; // Non damageable item
	}

	/** Gets the max damage/durability of an item
	 * <p>NOTE: Will account for custom damageable items in MC 1.20.5+</p>
	 * @param itemStack Item to grab durability from
	 * @return Max amount of damage this item can take
	 */
	public static int getMaxDamage(ItemStack itemStack) {
		ItemMeta meta = itemStack.getItemMeta();
		if (HAS_MAX_DAMAGE && meta instanceof Damageable && ((Damageable) meta).hasMaxDamage())
			return ((Damageable) meta).getMaxDamage();
		return itemStack.getType().getMaxDurability();
	}

	/**
	 * Set the max damage/durability of an item
	 *
	 * @param itemStack ItemStack to set max damage
	 * @param maxDamage Amount of new max damage
	 */
	public static void setMaxDamage(ItemStack itemStack, int maxDamage) {
		ItemMeta meta = itemStack.getItemMeta();
		if (HAS_MAX_DAMAGE && meta instanceof Damageable) {
			Damageable damageable = (Damageable) meta;
			if (HAS_RESET && maxDamage < 1) {
				damageable.resetDamage();
			} else {
				damageable.setMaxDamage(Math.max(1, maxDamage));
			}
			itemStack.setItemMeta(damageable);
		}
	}

	/**
	 * Sets damage/durability of an item if possible.
	 * @param itemStack Item to modify.
	 * @param damage New damage. Note that on some Minecraft versions,
	 * this might be truncated to short.
	 */
	public static void setDamage(ItemStack itemStack, int damage) {
		ItemMeta meta = itemStack.getItemMeta();
		if (meta instanceof Damageable) {
			((Damageable) meta).setDamage(Math.max(0, damage));
			itemStack.setItemMeta(meta);
		}
	}

	/**
	 * Gets damage/durability of an item, or 0 if it does not have damage.
	 * @param itemType Item.
	 * @return Damage.
	 */
	public static int getDamage(ItemType itemType) {
		ItemMeta meta = itemType.getItemMeta();
		if (meta instanceof Damageable)
			return ((Damageable) meta).getDamage();
		return 0; // Non damageable item
	}

	/** Gets the max damage/durability of an item
	 * <p>NOTE: Will account for custom damageable items in MC 1.20.5+</p>
	 * @param itemType Item to grab durability from
	 * @return Max amount of damage this item can take
	 */
	public static int getMaxDamage(ItemType itemType) {
		ItemMeta meta = itemType.getItemMeta();
		if (HAS_MAX_DAMAGE && meta instanceof Damageable && ((Damageable) meta).hasMaxDamage())
			return ((Damageable) meta).getMaxDamage();
		return itemType.getMaterial().getMaxDurability();
	}

	/**
	 * Sets damage/durability of an item if possible.
	 * @param itemType Item to modify.
	 * @param damage New damage. Note that on some Minecraft versions,
	 * this might be truncated to short.
	 */
	public static void setDamage(ItemType itemType, int damage) {
		ItemMeta meta = itemType.getItemMeta();
		if (meta instanceof Damageable) {
			((Damageable) meta).setDamage(damage);
			itemType.setItemMeta(meta);
		}
	}

	/**
	 * Sets the owner of a player head.
	 * @param skull player head item to modify
	 * @param player owner of the head
	 */
	public static void setHeadOwner(ItemType skull, OfflinePlayer player) {
		ItemMeta meta = skull.getItemMeta();
		if (!(meta instanceof SkullMeta))
			return;

		SkullMeta skullMeta = (SkullMeta) meta;

		if (REQUIRES_TEXTURE_LOOKUP) {
			PlayerProfile profile = player.getPlayerProfile();
			if (!profile.hasTextures())
				profile.complete(true); // BLOCKING MOJANG API CALL
			skullMeta.setPlayerProfile(profile);
		} else if (player.getName() != null) {
			skullMeta.setOwningPlayer(player);
		} else if (CAN_CREATE_PLAYER_PROFILE) {
			//noinspection deprecation
			skullMeta.setOwnerProfile(Bukkit.createPlayerProfile(player.getUniqueId(), ""));
		} else {
			skullMeta.setOwningPlayer(null);
		}

		skull.setItemMeta(skullMeta);
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
	 * @deprecated This just returns itself and has no use
	 */
	@Deprecated
	public static Material asItem(Material type) {
		// Assume (naively) that all types are valid items
		return type;
	}

	/**
	 * Convert an ItemType/Slot to ItemStack
	 * Will also accept an ItemStack that will return itself
	 *
	 * @param object Object to convert
	 * @return ItemStack from slot/itemtype
	 */
	@Nullable
	public static ItemStack asItemStack(Object object) {
		if (object instanceof ItemType)
			return ((ItemType) object).getRandom();
		else if (object instanceof Slot)
			return ((Slot) object).getItem();
		else if (object instanceof ItemStack)
			return ((ItemStack) object);
		return null;
	}
	
	/**
	 * Tests whether two item stacks are of the same type, i.e. it ignores the amounts.
	 *
	 * @param itemStack1
	 * @param itemStack2
	 * @return Whether the item stacks are of the same type
	 */
	public static boolean itemStacksEqual(@Nullable ItemStack itemStack1, @Nullable ItemStack itemStack2) {
		if (itemStack1 == null || itemStack2 == null)
			return itemStack1 == itemStack2;
		if (itemStack1.getType() != itemStack2.getType())
			return false;

		ItemMeta itemMeta1 = itemStack1.getItemMeta();
		ItemMeta itemMeta2 = itemStack2.getItemMeta();
		if (itemMeta1 == null || itemMeta2 == null)
			return itemMeta1 == itemMeta2;

		return itemStack1.getItemMeta().equals(itemStack2.getItemMeta());
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


	private static final boolean HAS_FENCE_TAGS = !Skript.isRunningMinecraft(1, 14);

	/**
	 * Whether the block is a fence or a wall.
	 * @param block the block to check.
	 * @return whether the block is a fence/wall.
	 */
	public static boolean isFence(Block block) {
		// TODO: 1.13 only, so remove in 2.10
		if (!HAS_FENCE_TAGS) {
			BlockData data = block.getBlockData();
			return data instanceof Fence
				|| data instanceof Wall
				|| data instanceof Gate;
		}

		Material type = block.getType();
		return Tag.FENCES.isTagged(type)
			|| Tag.FENCE_GATES.isTagged(type)
			|| Tag.WALLS.isTagged(type);
	}

	/**
	 * @param material The material to check
	 * @return whether the material is a full glass block
	 */
	public static boolean isGlass(Material material) {
		switch (material) {
			case GLASS:
			case RED_STAINED_GLASS:
			case ORANGE_STAINED_GLASS:
			case YELLOW_STAINED_GLASS:
			case LIGHT_BLUE_STAINED_GLASS:
			case BLUE_STAINED_GLASS:
			case CYAN_STAINED_GLASS:
			case LIME_STAINED_GLASS:
			case GREEN_STAINED_GLASS:
			case MAGENTA_STAINED_GLASS:
			case PURPLE_STAINED_GLASS:
			case PINK_STAINED_GLASS:
			case WHITE_STAINED_GLASS:
			case LIGHT_GRAY_STAINED_GLASS:
			case GRAY_STAINED_GLASS:
			case BLACK_STAINED_GLASS:
			case BROWN_STAINED_GLASS:
				return true;
			default:
				return false;
		}
	}
}
