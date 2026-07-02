package org.skriptlang.skript.bukkit.bossbar;

import ch.njol.skript.util.Color;
import ch.njol.skript.util.SkriptColor;
import org.bukkit.boss.BarColor;
import org.skriptlang.skript.util.LabColor;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Comparator;

/**
 * A utility class for boss bars to help with getting RGB from SkriptColor and rounding an RGB to the nearest BarColor.
 */
public final class BossBarUtils {

	private BossBarUtils() {}

	/**
	 * All the skript colors for each bar color
	 */
	enum BarColorRGB {

		PINK(BarColor.PINK, SkriptColor.LIGHT_RED),
		BLUE(BarColor.BLUE, SkriptColor.DARK_BLUE),
		RED(BarColor.RED, SkriptColor.DARK_RED),
		GREEN(BarColor.GREEN, SkriptColor.LIGHT_GREEN),
		YELLOW(BarColor.YELLOW, SkriptColor.YELLOW),
		PURPLE(BarColor.PURPLE, SkriptColor.DARK_PURPLE),
		WHITE(BarColor.WHITE, SkriptColor.WHITE);

		final BarColor barColor;
		final Color color;

		BarColorRGB(BarColor barColor, SkriptColor color) {
			this.barColor = barColor;
			this.color = color;
		}
	}

	/**
	 * Gets an RGB from a bar color cause well bukkit doesn't have it (& reasonably so)
	 * @param color the bar color to get the RGB color for
	 * @return the corresponding RGB color, or null
	 */
	public static Color rgbFromBarColor(BarColor color) {
		return Arrays.stream(BarColorRGB.values())
			.filter(c -> c.barColor == color)
			.map(c -> c.color)
			.findFirst()
			.orElse(null);
	}

	/**
	 * Gets the nearest bossbar color from a skript color
	 * @param color the RGB color to find the nearest bar color for
	 * @return the nearest bar color, or null if no close enough match is found
	 */
	public static @Nullable BarColor nearest(Color color) {
		LabColor lab = LabColor.fromRGB(color.getRed(), color.getGreen(), color.getBlue());
		var value = Arrays.stream(BarColorRGB.values())
			.min(Comparator.comparingDouble(c -> lab.euclideanDistanceSquared(
				LabColor.fromRGB(c.color.getRed(), c.color.getGreen(), c.color.getBlue()))));
		if (value.isEmpty())
			return null;
		LabColor bestLab = LabColor.fromRGB(value.get().color.getRed(), value.get().color.getGreen(), value.get().color.getBlue());
		return lab.euclideanDistanceSquared(bestLab) < 2500 ? value.get().barColor : null;
	}

}
