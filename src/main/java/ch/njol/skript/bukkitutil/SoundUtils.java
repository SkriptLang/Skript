package ch.njol.skript.bukkitutil;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * Utility class for working with sounds.
 */
public final class SoundUtils {

	// Sound.class is an interface (rather than an enum) as of MC 1.21.3
	private static final boolean SOUND_IS_INTERFACE = Sound.class.isInterface();

	/**
	 * Gets the key of a sound, given its enum-name-style name.
	 * @param soundString The enum name to use to find the sound.
	 * @return The key of the sound.
	 */
	public static @Nullable NamespacedKey getKey(String soundString) {
		soundString = soundString.toUpperCase(Locale.ENGLISH);
		if (SOUND_IS_INTERFACE) {
			try {
				//noinspection deprecation
				return Sound.valueOf(soundString).getKey();
			} catch (IllegalArgumentException ignore) {
			}
		} else {
			try {
				//noinspection unchecked,rawtypes
				Enum soundEnum = Enum.valueOf((Class) Sound.class, soundString);
				return ((Keyed) soundEnum).getKey();
			} catch (IllegalArgumentException ignore) {
			}
		}
		return null;
	}

	/**
	 * returns the key string for a sound. For version compat.
	 * @param sound The sound to get the key string of.
	 * @return The key string of the {@link NamespacedKey} of the sound.
	 */
	public static @NotNull NamespacedKey getKey(Sound sound) {
		if (SOUND_IS_INTERFACE) {
			//noinspection deprecation
			return sound.getKey();
		} else {
			return ((Keyed) sound).getKey();
		}
	}

}
