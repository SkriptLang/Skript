package ch.njol.skript.util;

import ch.njol.util.Math2;
import org.bukkit.DyeColor;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.common.colors.ColorUtils;

/**
 /**
 * Immutable representation of a color in the HSB/HSV color-space, with an alpha channel.
 * Hue, saturation, and brightness are stored in normalized form: 0-1.
 * Alpha is stored as a value from 0-255 (like the red, green, and blue channels).
 */
public final class ColorHSB implements Color {

	private final float hue;
	private final float saturation;
	private final float brightness;
	private final int alpha;

	private final ColorRGB rgb;
	private final @Nullable DyeColor dye;

	private ColorHSB(float hue, float saturation, float brightness, int alpha) {
		this.hue = hue;
		this.saturation = saturation;
		this.brightness = brightness;
		this.alpha = alpha;

		this.rgb = ColorUtils.hsbToRgb(this);
		this.dye = rgb.asDyeColor();
	}

	public static @NotNull ColorHSB fromHSBA(float hue, float saturation, float brightness, int alpha) {
		return new ColorHSB(
			Math2.fit(0f, hue, 1f),
			Math2.fit(0f, saturation, 1f),
			Math2.fit(0f, brightness, 1f),
			Math2.fit(0, alpha, 255)
		);
	}

	public static @NotNull ColorHSB fromHSB(float hue, float saturation, float brightness) {
		return fromHSBA(hue, saturation, brightness, 255);
	}

	@ApiStatus.Internal
	public static @NotNull ColorHSB fromUncheckedHSBA(float hue, float saturation, float brightness, int alpha) {
		return new ColorHSB(hue, saturation, brightness, alpha);
	}

	public float getHue() {
		return hue;
	}

	public float getSaturation() {
		return saturation;
	}

	public float getBrightness() {
		return brightness;
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
		String hsb = String.format("%.3f, %.3f, %.3f", hue, saturation, brightness);
		return alpha == 255 ? "hsb " + hsb : "hsba " + hsb + ", " + alpha;
	}

}
