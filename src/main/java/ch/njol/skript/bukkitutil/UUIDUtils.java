package ch.njol.skript.bukkitutil;

import ch.njol.util.StringUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Utility class for quick {@link UUID} methods.
 */
public class UUIDUtils {

	/**
	 * Converts {@code object} into a UUID if possible
	 * @param object
	 * @return The resulting {@link UUID}
	 */
	public static @Nullable UUID asUUID(@NotNull Object object) {
		if (object instanceof OfflinePlayer offlinePlayer) {
			return offlinePlayer.getUniqueId();
		} else if (object instanceof Entity entity) {
			return entity.getUniqueId();
		} else if (object instanceof String string && StringUtils.containsAny(string, "-")) {
			try {
				return UUID.fromString(string);
			} catch (Exception ignored) {}
		} else if (object instanceof UUID uuid) {
			return uuid;
		}
		return null;
	}

}
