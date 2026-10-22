package ch.njol.skript.bukkitutil;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.Nullable;

/**
 * Contains helpers for Bukkit's not so safe stuff.
 */
public class BukkitUnsafe {

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

}
