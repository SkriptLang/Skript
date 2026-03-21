package ch.njol.skript.util;

import ch.njol.yggdrasil.YggdrasilSerializable.YggdrasilExtendedSerializable;
import org.bukkit.DyeColor;
import org.jetbrains.annotations.Nullable;

public interface Color extends YggdrasilExtendedSerializable {

	/**
	 * Gets Bukkit color representing this color.
	 * @return Bukkit color.
	 */
	org.bukkit.Color asBukkitColor();

	/**
	 * @return The alpha component of this color.
	 */
	int getAlpha();

	/**
	 * @return The red component of this color.
	 */
	int getRed();

	/**
	 * @return The green component of this color.
	 */
	int getGreen();

	/**
	 * @return The blue component of this color.
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

	/**
	 * @return the colour as an RGB hex value: RRGGBB
	 */
	default String toHexString() {
		return String.format("%02X%02X%02X", getRed(), getGreen(), getBlue());
	}

}
