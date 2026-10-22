package ch.njol.skript.bukkitutil;

import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.UnsafeValues;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Contains helpers for Bukkit's not so safe stuff.
 */
@SuppressWarnings("deprecation")
public class BukkitUnsafe {

	/**
	 * Bukkit's UnsafeValues allows us to do stuff that would otherwise
	 * require NMS. It has existed for a long time, too, so 1.9 support is
	 * not particularly hard to achieve.
	 *
	 * UnsafeValues' existence and behavior is not guaranteed across future versions.
	 */
	private static final @Nullable UnsafeValues unsafe = Bukkit.getUnsafe();

	static {
		if (unsafe == null)
			throw new Error("UnsafeValues are not available.");
	}

	/**
	 * Get a material from a minecraft id.
	 *
	 * @param id Namespaced ID with or without a namespace. IDs without a namespace will be treated
	 * 		as minecraft namespaced IDs. ('minecraft:dirt' and 'dirt' are equivalent.)
	 * @return The Material which the id represents, or null if no material can be matched.
	 * @deprecated Prefer {@link BukkitUnsafe#getMaterialFromNamespacedId(String)} for including modded item support
	 */
	@Deprecated(since = "2.10.0", forRemoval = true)
	public static @Nullable Material getMaterialFromMinecraftId(String id) {
		return getMaterialFromNamespacedId(id);
	}

	/**
	 * Get a material from a namespaced ID.
	 * For example, 'minecraft:iron_ingot' -> Material.IRON_INGOT; 'mod:an_item' -> Material.MOD_AN_ITEM
	 *
	 * @param id Namespaced ID with or without a namespace. IDs without a namespace will be treated
	 * 		as minecraft namespaced IDs. ('minecraft:dirt' and 'dirt' are equivalent.)
	 * @return The Material which the id represents, or null if no material can be matched.
	 */
	public static @Nullable Material getMaterialFromNamespacedId(String id) {
		return Material.matchMaterial(id.toLowerCase().startsWith(NamespacedKey.MINECRAFT + ":")
										  ? id
										  : id.replace(":", "_")  //For Hybrid Server
		);
	}

	public static void modifyItemStack(ItemStack stack, String arguments) {
		if (unsafe == null)
			throw new IllegalStateException("modifyItemStack could not be performed as UnsafeValues are not available.");
		unsafe.modifyItemStack(stack, arguments);
	}

}
