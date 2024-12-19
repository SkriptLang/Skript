package ch.njol.skript.bukkitutil;

import ch.njol.skript.Skript;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NamespacedUtils {

	/**
	 * Gets a {@link NamespacedKey} from the provided {@code key}.
	 * If {@code key} contains a namespace identifier within, that will be the namespace.
	 * Otherwise, it will create the key in Skript's namespace
	 *
	 * @param key The unparsed key
	 * @return The resulting {@link NamespacedKey}
	 */
	public static @Nullable NamespacedKey getNamespacedKey(@NotNull String key) {
		return getNamespacedKey(key, true);
	}

	/**
	 * Gets a {@link NamespacedKey} from the provided {@code key}.
	 * If {@code key} contains a namespace identifier within, that will be the namespace.
	 * Otherwise, it will create the key in Skript's namespace if {@code skriptNamespace} is true, or Minecraft's if false.
	 *
	 * @param key The unparsed key
	 * @param skriptNamespace If the key should be created in Skript's namespace or Minecraft's
	 * @return The resulting {@link NamespacedKey}
	 */
	public static @Nullable NamespacedKey getNamespacedKey(@NotNull String key, boolean skriptNamespace) {
		NamespacedKey namespacedKey;
		if (skriptNamespace)
			namespacedKey = NamespacedKey.fromString(key, Skript.getInstance());
		else
			namespacedKey = NamespacedKey.fromString(key);
		if (namespacedKey != null)
			return namespacedKey;

		return null;
	}

}
