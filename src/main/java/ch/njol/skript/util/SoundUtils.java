package ch.njol.skript.util;

import ch.njol.skript.Skript;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;

public class SoundUtils {

	private static final boolean IS_INTERFACE;
	private static final Method VALUE_OF_METHOD;
	private static final Method GET_KEY_METHOD;

	static {
		try {
			Class<?> SOUND_CLASS = Class.forName("org.bukkit.Sound");
			IS_INTERFACE = SOUND_CLASS.isInterface();
			VALUE_OF_METHOD = SOUND_CLASS.getDeclaredMethod("valueOf", String.class);
			if (Skript.methodExists(SOUND_CLASS, "getKey")) {
				// I believe this method was added around Bukkit 1.16
				// This is only added to make tests not fail when testing MC 1.15 and below
				GET_KEY_METHOD = SOUND_CLASS.getDeclaredMethod("getKey");
			} else {
				GET_KEY_METHOD = null;
			}
		} catch (NoSuchMethodException | ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Get the NamespacedKey of a Bukkit Sound enum
	 *
	 * @param soundString String to check for enum
	 * @return Sound key if available else null
	 */
	@SuppressWarnings("deprecation")
	@Nullable
	public static NamespacedKey getSoundKeyFromEnum(String soundString) {
		soundString = soundString.toUpperCase(Locale.ENGLISH);
		// Sound.class is an Interface (rather than an enum) as of MC 1.21.3
		if (IS_INTERFACE) {
			try {
				Sound sound = Sound.valueOf(soundString);
				return sound.getKey();
			} catch (IllegalArgumentException ignore) {
			}
		} else if (GET_KEY_METHOD != null) {
			try {
				Object sound = VALUE_OF_METHOD.invoke(null, soundString);
				if (sound != null) {
					return (NamespacedKey) GET_KEY_METHOD.invoke(sound);
				}
			} catch (IllegalAccessException |
					 InvocationTargetException ignore) {
			}
		}
		return null;
	}

}
