package ch.njol.skript.util;

import ch.njol.skript.Skript;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Keyed;
import org.bukkit.Registry;
import org.jetbrains.annotations.Nullable;

public class PaperUtils {

	private static final boolean REGISTRY_ACCESS_EXISTS = Skript.classExists("io.papermc.paper.registry.RegistryAccess");
	private static final boolean REGISTRY_KEY_EXISTS = Skript.classExists("io.papermc.paper.registry.RegistryKey");

	/**
	 * Check if a registry exists within {@link RegistryKey}.
	 * @param registry Registry to check for.
	 * @return True if registry exists else false.
	 */
	public static boolean registryExists(String registry) {
		return REGISTRY_ACCESS_EXISTS
			&& REGISTRY_KEY_EXISTS
			&& Skript.fieldExists(RegistryKey.class, registry);
	}

	/**
	 * Gets a Paper {@link RegistryKey}.
	 * @param registry Registry key to get.
	 * @return The Bukkit {@link Registry} if registry exists else {@code null}.
	 */
	public static <T extends Keyed> @Nullable RegistryKey<T> getBukkitRegistryKey(String registry) {
		if (!registryExists(registry))
			return null;
        RegistryKey<T> registryKey;
        try {
			//noinspection unchecked
			registryKey = (RegistryKey<T>) RegistryKey.class.getField(registry).get(null);
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
            return null;
        }
		return registryKey;
	}

	/**
	 * Gets the Bukkit {@link Registry} from Paper's {@link RegistryKey}.
	 * @param registry Registry to get.
	 * @return The Bukkit {@link Registry} if registry exists else {@code null}.
	 */
	public static <T extends Keyed> @Nullable Registry<T> getBukkitRegistry(String registry) {
		RegistryKey<T> registryKey = getBukkitRegistryKey(registry);
		if (registryKey == null) {
			return null;
		}
		return RegistryAccess.registryAccess().getRegistry(registryKey);
	}

}
