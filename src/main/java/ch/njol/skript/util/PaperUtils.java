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

	public static boolean registryExists(String registry) {
		return REGISTRY_ACCESS_EXISTS
			&& REGISTRY_KEY_EXISTS
			&& Skript.fieldExists(RegistryKey.class, registry);
	}

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
