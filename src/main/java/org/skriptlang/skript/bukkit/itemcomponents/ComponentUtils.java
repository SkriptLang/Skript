package org.skriptlang.skript.bukkit.itemcomponents;

import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.set.RegistryKeySet;
import io.papermc.paper.registry.set.RegistrySet;
import org.bukkit.Keyed;
import org.bukkit.Registry;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

/**
 * Utility class for components.
 */
@SuppressWarnings("UnstableApiUsage")
public class ComponentUtils {

	/**
	 * Convert a {@link RegistryKeySet} to a {@link Collection}.
	 * @param registryKeySet The {@link RegistryKeySet} to convert.
	 * @param registry The {@link Registry} type of {@code registryKeySet}
	 * @return The converted {@link Collection}.
	 */
	public static <T extends Keyed> Collection<T> registryKeySetToCollection(
		@Nullable RegistryKeySet<T> registryKeySet,
		Registry<T> registry
	) {
		if (registryKeySet == null || registryKeySet.isEmpty())
			return Collections.emptyList();
		return registryKeySet.resolve(registry);
	}

	/**
	 * Convert a {@link Collection} to a {@link RegistryKeySet}.
	 * @param collection The {@link Collection} to convert.
	 * @param registryKey The {@link RegistryKey} type to convert to.
	 * @return The converted {@link RegistryKeySet}.
	 */
	public static <T extends Keyed> RegistryKeySet<T> collectionToRegistryKeySet(
		Collection<T> collection,
		RegistryKey<T> registryKey
	) {
		return RegistrySet.keySetFromValues(registryKey, collection);
	}

}
