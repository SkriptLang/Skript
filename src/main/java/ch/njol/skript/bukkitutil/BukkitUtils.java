package ch.njol.skript.bukkitutil;

import ch.njol.skript.Skript;
import org.bukkit.Registry;

/**
 * Utility class with methods pertaining to Bukkit API
 */
public class BukkitUtils {

	/**
	 * Check if a registry exists
	 *
	 * @param registry Registry to check for (Fully qualified name of registry)
	 * @return True if registry exists else false
	 */
	public static boolean registryExists(String registry) {
		return Skript.classExists("org.bukkit.Registry") && Skript.fieldExists(Registry.class, registry);
	}

}
