package ch.njol.skript.bukkitutil;

import ch.njol.skript.Skript;
import org.bukkit.Registry;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

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

	/**
	 * Get an instance of the {@link PotionEffectType} {@link Registry}
	 * <p>Paper/Bukkit have 2 different names for the same registry.</p>
	 *
	 * @return PotionEffectType Registry
	 */
	@SuppressWarnings("NullableProblems")
	public static @Nullable Registry<PotionEffectType> getPotionEffectTypeRegistry() {
		if (registryExists("MOB_EFFECT")) { // Paper (1.21.4)
			return Registry.MOB_EFFECT;
		} else if (registryExists("EFFECT")) { // Bukkit (1.21.x)
			return Registry.EFFECT;
		}
		return null;
	}

}
