package org.skriptlang.skript.bukkit.itemcomponents;

import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.set.RegistryKeySet;
import io.papermc.paper.registry.set.RegistrySet;
import org.bukkit.Keyed;
import org.bukkit.Registry;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

@SuppressWarnings("UnstableApiUsage")
public class ComponentUtils {

	public static <T extends Keyed> Collection<T> registryKeySetToCollection(
		@Nullable RegistryKeySet<T> registryKeySet,
		Registry<T> registry
	) {
		if (registryKeySet == null || registryKeySet.isEmpty())
			return Collections.emptyList();
		return registryKeySet.resolve(registry);
	}

	public static <T extends Keyed> RegistryKeySet<T> collectionToRegistryKeySet(
		Collection<T> collection,
		RegistryKey<T> registryKey
	) {
		return RegistrySet.keySetFromValues(registryKey, collection);
	}

}
