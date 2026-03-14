package ch.njol.skript.util;

import org.bukkit.DyeColor;
import org.jetbrains.annotations.Nullable;

public interface Color {

	/**
	 * @return The Bukkit color representing this color.
	 */
	org.bukkit.Color asBukkitColor();

	/**
	 * @return The alpha channel of this color.
	 */
	int getAlpha();

	/**
	 * @return The red channel of this color.
	 */
	int getRed();

	/**
	 * @return The green channel of this color.
	 */
	int getGreen();

	/**
	 * @return The blue channel of this color.
	 */
	int getBlue();

	/**
	 * @return The {@link DyeColor} representing this color if one exists, or null otherwise.
	 */
	@Nullable DyeColor asDyeColor();

	/**
	 * @return Name of the color.
	 */
	String getName();

	/**
	 * @return The color as an ARGB integer.
	 */
	default int asARGB() {
		return asBukkitColor().asARGB();
	}

	/**
	 * @return The color as an RGB hex value: RRGGBB
	 */
	default String toHexString() {
		return String.format("%02X%02X%02X", getRed(), getGreen(), getBlue());
	}

	/**
	 * @return The integer representing this color. Used for serialization.
	 */
	default int asInt() {
		return (getAlpha() << 24) | (getRed() << 16) | (getGreen() << 8) | getBlue();
	}

}
