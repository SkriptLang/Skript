package ch.njol.skript.util;

import org.bukkit.DyeColor;
import org.jetbrains.annotations.Nullable;

public interface Color {

	/**
	 * @return The Bukkit color representing this color.
	 */
	org.bukkit.Color asBukkitColor();

	/**
	 * @return The hexadecimal code representing this color. Can be used to color text.
	 */
	default String getHex() {
		return String.format("#%02X%02X%02X", getRed(), getGreen(), getBlue());
	}

	/**
	 * @return The hexadecimal code representing this color, including the alpha channel. Cannot be used to color text.
	 */
	default String getFullHex() {
		return String.format("#%02X%02X%02X%02X", getAlpha(), getRed(), getGreen(), getBlue());
	}

	/**
	 * @return The integer representing this color.
	 */
	default int asInt() {
		return (getAlpha() << 24) | (getRed() << 16) | (getGreen() << 8) | getBlue();
	}

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
	 * Gets Bukkit dye color representing this color, if one exists.
	 * @return Dye color or null.
	 */
	@Nullable
	DyeColor asDyeColor();

	/**
	 * @return Name of the color.
	 */
	String getName();

	/**
	 * @return the color as an ARGB integer.
	 */
	default int asARGB() {
		return asBukkitColor().asARGB();
	}

}
