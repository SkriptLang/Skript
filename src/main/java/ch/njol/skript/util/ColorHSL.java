package ch.njol.skript.util;

import ch.njol.util.Math2;
import org.bukkit.DyeColor;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.common.colors.ColorUtils;

/**
 * Immutable representation of a color in the HSL color-space, with an alpha channel.
 * Hue, saturation, and lightness are stored in normalized form: 0-1.
 * Alpha is stored as a value from 0-255 (like the red, green, and blue channels).
 */
public class ColorHSL implements Color {

	private final float hue;
	private final float saturation;
	private final float lightness;
	private final int alpha;

	private final ColorRGB rgb;
	private final @Nullable DyeColor dye;

	private ColorHSL(float hue, float saturation, float lightness, int alpha) {
		this.hue = hue;
		this.saturation = saturation;
		this.lightness = lightness;
		this.alpha = alpha;

		this.rgb = ColorUtils.hslToRgb(this);
		this.dye = rgb.asDyeColor();
	}

	public static @NotNull ColorHSL fromHSLA(float hue, float saturation, float lightness, int alpha) {
		return new ColorHSL(
			Math2.fit(0f, hue, 1f),
			Math2.fit(0f, saturation, 1f),
			Math2.fit(0f, lightness, 1f),
			Math2.fit(0, alpha, 255)
		);
	}

	public static @NotNull ColorHSL fromHSL(float hue, float saturation, float lightness) {
		return fromHSLA(hue, saturation, lightness, 255);
	}

	@ApiStatus.Internal
	public static @NotNull ColorHSL fromUncheckedHSLA(float hue, float saturation, float lightness, int alpha) {
		return new ColorHSL(hue, saturation, lightness, alpha);
	}

	public float getHue() {
		return hue;
	}

	public float getSaturation() {
		return saturation;
	}

	public float getLightness() {
		return lightness;
	}

	@Override
	public org.bukkit.Color asBukkitColor() {
		return rgb.asBukkitColor();
	}

	@Override
	public int getAlpha() {
		return alpha;
	}

	@Override
	public int getRed() {
		return rgb.getRed();
	}

	@Override
	public int getGreen() {
		return rgb.getGreen();
	}

	@Override
	public int getBlue() {
		return rgb.getBlue();
	}

	@Override
	public @Nullable DyeColor asDyeColor() {
		return dye;
	}

	@Override
	public String getName() {
		String hslString = String.format("%.3f, %.3f, %.3f", hue, saturation, lightness);
		if (alpha != 255)
			return "hsla " + hslString + ", " + alpha;
		return "hsl " + hslString;
	}

}
