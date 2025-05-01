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
	 * Check if a registry exists within {@link RegistryKey}
	 * @param registry Registry to check for (Fully qualified name of registry)
	 * @return True if registry exists else false
	 */
	public static boolean registryExists(String registry) {
		return REGISTRY_ACCESS_EXISTS
			&& REGISTRY_KEY_EXISTS
			&& Skript.fieldExists(RegistryKey.class, registry);
	}

	/**
	 * Gets the Bukkit {@link Registry} from Paper's {@link RegistryKey}.
	 * @param registry Registry to get (Fully qualified name of registry).
	 * @return The Bukkit {@link Registry} if registry exists else {@code null}.
	 */
	public static <T extends Keyed> @Nullable Registry<T> getBukkitRegistry(String registry) {
		if (!registryExists(registry))
			return null;
        RegistryKey registryKey;
        try {
			registryKey = (RegistryKey) RegistryKey.class.getField(registry).get(null);
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
            return null;
        }
		//noinspection unchecked
		return (Registry<T>) RegistryAccess.registryAccess().getRegistry(registryKey);
	}

}
