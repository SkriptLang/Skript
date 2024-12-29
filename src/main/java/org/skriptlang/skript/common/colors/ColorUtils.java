package org.skriptlang.skript.common.colors;

import ch.njol.skript.util.Color;
import ch.njol.skript.util.ColorRGB;
import ch.njol.skript.util.SkriptColor;
import ch.njol.util.Math2;
import org.jetbrains.annotations.NotNull;

/**
 * Utility class for color manipulation and conversion.
 */
public class ColorUtils {

	/**
	 * Converts an integer representation of a color to its {@link Color} equivalent.
	 *
	 * @param asInt an integer representing a color
	 * @return the {@link Color} represented by the integer
	 */
	public static Color fromInt(int asInt) {
		for (SkriptColor preset : SkriptColor.values()) {
			if (preset.asInt() == asInt) {
				return preset;
			}
		}
		int alpha = (asInt >> 24) & 0xFF;
		int red = (asInt >> 16) & 0xFF;
		int green = (asInt >> 8) & 0xFF;
		int blue = asInt & 0xFF;
		return ColorRGB.fromRGBA(red, green, blue, alpha);
	}

	/**
	 * Converts a hex code representation of a color to its {@link Color} equivalent.
	 *
	 * @param hex a hex code representing a color
	 * @return the {@link Color} represented by the hex code
	 *
	 * @throws IllegalArgumentException if the hex string is not of a supported format
	 */
	public static ColorRGB fromHex(@NotNull String hex) {
		if (hex.startsWith("#"))
			hex = hex.substring(1);

		int length = hex.length();
		int alpha = 255, red, green, blue;

		if (length == 6) {
			red = Integer.parseInt(hex.substring(0, 2), 16);
			green = Integer.parseInt(hex.substring(2, 4), 16);
			blue = Integer.parseInt(hex.substring(4, 6), 16);
		} else if (length == 8) {
			alpha = Integer.parseInt(hex.substring(0, 2), 16);
			red = Integer.parseInt(hex.substring(2, 4), 16);
			green = Integer.parseInt(hex.substring(4, 6), 16);
			blue = Integer.parseInt(hex.substring(6, 8), 16);
		} else {
			throw new UnsupportedOperationException("Unsupported hex format - requires #RRGGBB or #AARRGGBB");
		}

		return ColorRGB.fromRGBA(red, green, blue, alpha);
	}

	/**
	 * Converts a {@link Color} to HSL (hue, saturation, lightness).
	 *
	 * @param color the {@link Color} to convert
	 * @return a float array representing the HSL values
	 */
	public static float @NotNull [] rgbToHsl(@NotNull Color color) {
		// normalize rgb to between 0 and 1
		float red = color.getRed() / 255f;
		float green = color.getGreen() / 255f;
		float blue = color.getBlue() / 255f;

		float max = Math.max(red, Math.max(green, blue));
		float min = Math.min(red, Math.min(green, blue));

		float hue, saturation, lightness = (max + min) / 2f; // lightness = midpoint of max and min

		if (max == min) {
			// achromatic (no hue or saturation)
			hue = saturation = 0f;
		} else {
			float delta = max - min;

			// saturation depends on lightness (scales differently if lightness > 0.5)
			saturation = lightness > 0.5f ? delta / (2f - max - min) : delta / (max + min);

			// determine hue by which channel is max
			// normalize hue by converting from a 0-360 scale to a 0-1 scale by dividing by 6
			if (max == red) {
				hue = ((green - blue) / delta + (green < blue ? 6f : 0f)) / 6f;
			} else if (max == green) {
				hue = ((blue - red) / delta + 2f) / 6f;
			} else {
				hue = ((red - green) / delta + 4f) / 6f;
			}
		}
		return new float[]{ hue, saturation, lightness };
	}

	/**
	 * Converts HSL values to a {@link ColorRGB} object.
	 *
	 * @param hsl a float array representing HSL values
	 * @return a {@link ColorRGB} object given the HSL values
	 */
	public static @NotNull ColorRGB hslToRgb(float @NotNull [] hsl) {
		float hue = hsl[0], saturation = hsl[1], lightness = hsl[2];
		float red, green, blue;
		if (saturation == 0f) {
			// achromatic i.e. gray (all channels equal to lightness)
			red = green = blue = lightness;
		} else {
			// higherBound and lowerBound define two boundary colors
			float lowerBound = lightness < 0.5f ? lightness * (1f + saturation) : (lightness + saturation) - (lightness * saturation);
			float higherBound = 2f * lightness - lowerBound;
			red = hueToRgb(higherBound, lowerBound, hue + 1f / 3f);
			green = hueToRgb(higherBound, lowerBound, hue);
			blue = hueToRgb(higherBound, lowerBound, hue - 1f / 3f);
		}
		int r = Math.round(red * 255f);
		int g = Math.round(green * 255f);
		int b = Math.round(blue * 255f);
		return ColorRGB.fromRGBA(r, g, b, 255);
	}

	/**
	 * Helper method to convert hue values to RGB.
	 *
	 * @param lowerBound intermediate value
	 * @param higherBound intermediate value
	 * @param hueOffset hue offset
	 * @return the calculated RGB value
	 */
	private static float hueToRgb(float lowerBound, float higherBound, float hueOffset) {
		// wrap hueOffset if out of 0-1 range
		if (hueOffset < 0f)
			hueOffset += 1f;
		if (hueOffset > 1f)
			hueOffset -= 1f;

		// depending on hueOffset, interpolate between lowerBound and higherBound
		if (hueOffset < 1f / 6f)
			return lowerBound + (higherBound - lowerBound) * 6f * hueOffset;
		if (hueOffset < 1f / 2f)
			return higherBound;
		if (hueOffset < 2f / 3f)
			return lowerBound + (higherBound - lowerBound) * (2f / 3f - hueOffset) * 6f;
		return lowerBound;
	}

	/**
	 * Blends two {@link Color}s based on an amount from 0 to 100.
	 *
	 * @param c1 the first {@link Color}
	 * @param c2 the second {@link Color}
	 * @param amount the percentage amount to blend the colours (0 - 100)
	 * @return the blended colour
	 */
	public static @NotNull Color blendColors(@NotNull Color c1, @NotNull Color c2, double amount) {
		// amount is a percentage (clamp then normalize to between 0 and 1)
		amount = Math2.fit(0, amount, 100) / 100.0;

		// linearly interpolate each channel
		int r = (int) (c1.getRed() * (1 - amount) + c2.getRed() * amount);
		int g = (int) (c1.getGreen() * (1 - amount) + c2.getGreen() * amount);
		int b = (int) (c1.getBlue() * (1 - amount) + c2.getBlue() * amount);
		int a = (int) (c1.getAlpha() * (1 - amount) + c2.getAlpha() * amount);
		return ColorRGB.fromRGBA(r, g, b, a);
	}

	/**
	 * Calculates the complement of a {@link Color}.
	 *
	 * @param color the {@link Color} to complement
	 * @return the complementary colour
	 */
	public static @NotNull Color complementColor(@NotNull Color color) {
		// just invert each channel
		int r = 255 - color.getRed();
		int g = 255 - color.getGreen();
		int b = 255 - color.getBlue();
		return ColorRGB.fromRGBA(r, g, b, color.getAlpha());
	}

	/**
	 * Calculates the complement of a {@link Color} using HSL adjustments.
	 *
	 * @param color the {@link Color} to complement
	 * @return the complementary colour
	 */
	public static @NotNull Color complementColorHSL(@NotNull Color color) {
		float[] hsl = rgbToHsl(color);
		// adding 0.5 flips hue by 180 (i.e. finds the complement)
		hsl[0] = (hsl[0] + 0.5f) % 1f;
		return hslToRgb(hsl);
	}

	/**
	 * Shades a {@link Color} by a given amount from 1 to 100.
	 *
	 * @param color the {@link Color} to shade
	 * @param amount the amount to shade the colour by (1 - 100)
	 * @return the shaded colour
	 */
	public static @NotNull ColorRGB shadeColor(@NotNull Color color, int amount) {
		// reducing the channel values darkens the color
		amount = Math2.fit(1, amount, 100);
		double factor = (100 - amount) / 100.0;
		int r = (int) (color.getRed() * factor);
		int g = (int) (color.getGreen() * factor);
		int b = (int) (color.getBlue() * factor);
		return ColorRGB.fromRGBA(r, g, b, color.getAlpha());
	}

	/**
	 * Shades a {@link Color} by a given amount from 1 to 100 using HSL adjustments.
	 *
	 * @param color the {@link Color} to shade using HSL adjustments
	 * @param amount the amount to shade the colour by (1 - 100)
	 * @return the shaded colour
	 */
	public static @NotNull ColorRGB shadeColorHSL(@NotNull Color color, int amount) {
		// reducing the lightness to shade
		amount = Math2.fit(1, amount, 100);
		float[] hsl = rgbToHsl(color);
		hsl[2] *= (100 - amount) / 100f;
		return hslToRgb(hsl);
	}

	/**
	 * Tints a {@link Color} by a given amount from 1 to 100.
	 *
	 * @param color the {@link Color} to tint
	 * @param amount the amount to tint the colour by (1 - 100)
	 * @return the tinted colour
	 */
	public static @NotNull ColorRGB tintColor(@NotNull Color color, int amount) {
		// move each channel closer to 255 to lighten the colour
		amount = Math2.fit(1, amount, 100);
		double factor = amount / 100.0;
		int r = (int) (color.getRed() + (255 - color.getRed()) * factor);
		int g = (int) (color.getGreen() + (255 - color.getGreen()) * factor);
		int b = (int) (color.getBlue() + (255 - color.getBlue()) * factor);
		return ColorRGB.fromRGBA(r, g, b, color.getAlpha());
	}

	/**
	 * Tints a {@link Color} by a given amount from 1 to 100 using HSL adjustments.
	 *
	 * @param color the {@link Color} to tint using HSL adjustments
	 * @param amount the amount to tint the colour by (1 - 100)
	 * @return the tinted colour
	 */
	public static @NotNull ColorRGB tintColorHSL(@NotNull Color color, int amount) {
		// increasing the lightness (towards 1) to tint
		amount = Math2.fit(1, amount, 100);
		float[] hsl = rgbToHsl(color);
		hsl[2] += (1f - hsl[2]) * (amount / 100f);
		hsl[2] = Math.min(1f, hsl[2]);
		return hslToRgb(hsl);
	}

	/**
	 * Rotates the hue of a {@link Color} by a given degree.
	 *
	 * @param color the {@link Color} to rotate the hue of
	 * @param degrees the number of degrees to rotate the hue by
	 * @return the hue-rotated colour
	 */
	public static @NotNull Color rotateHue(@NotNull Color color, int degrees) {
		// hue is a fraction of a circle, add (degrees/360) to rotate by that angle
		float[] hsl = rgbToHsl(color);
		hsl[0] = (hsl[0] + degrees / 360f) % 1f;
		if (hsl[0] < 0f)
			hsl[0] += 1f;
		return hslToRgb(hsl);
	}

	/**
	 * Adjusts the brightness of a {@link Color} by an amount from -100 to 100.
	 * This is similar to shading and tinting, but is slightly different.
	 *
	 * @param color the {@link Color} to adjust the brightness of
	 * @param amount the amount to adjust the brightness by (-100 - 100)
	 * @return the brightness-adjusted colour
	 */
	public static @NotNull ColorRGB adjustBrightness(@NotNull Color color, int amount) {
		// adjust brightness by scaling brightness directly (shocking ik)
		amount = Math2.fit(-100, amount, 100);
		float[] hsb = rgbToHsb(color);
		float factor = amount / 100f;
		hsb[2] = hsb[2] + hsb[2] * factor;
		hsb[2] = Math.max(0f, Math.min(1f, hsb[2]));
		return hsbToRgb(hsb);
	}

	/**
	 * Converts a {@link Color} to HSB (hue, saturation, brightness).
	 * This is different to {@link #rgbToHsl(Color)}.
	 *
	 * @param color the {@link Color} to convert
	 * @return a float array representing the HSB values
	 */
	private static float @NotNull [] rgbToHsb(@NotNull Color color) {
		// normalize rgb to between 0 and 1
		float r = color.getRed() / 255f;
		float g = color.getGreen() / 255f;
		float b = color.getBlue() / 255f;

		// max defines brightness, delta defines saturation
		float max = Math.max(r, Math.max(g, b));
		float min = Math.min(r, Math.min(g, b));
		float delta = max - min;
		float hue = 0f;
		float saturation = max == 0f ? 0f : delta / max;

		// hue depends on which channel is max
		if (delta != 0f) {
			if (max == r) {
				hue = ((g - b) / delta) % 6f;
			} else if (max == g) {
				hue = ((b - r) / delta) + 2f;
			} else {
				hue = ((r - g) / delta) + 4f;
			}
			hue *= 60f;
			if (hue < 0f)
				hue += 360f;
		}
		hue /= 360f;
		return new float[]{ hue, saturation, max};
	}

	/**
	 * Converts HSB values to a {@link ColorRGB} object.
	 * This is different to {@link #hslToRgb(float[])}.
	 *
	 * @param hsb a float array representing HSB values
	 * @return a {@link ColorRGB} object given the HSB values
	 */
	private static @NotNull ColorRGB hsbToRgb(float @NotNull [] hsb) {
		float hue = hsb[0], saturation = hsb[1], brightness = hsb[2];
		// determine hue sector and interpolate
		float red = 0f, green = 0f, blue = 0f;
		int hueSector = (int) (hue * 6f);
		float sectorFraction = hue * 6f - hueSector;
		float lowerBound = brightness * (1f - saturation);
		float higherBound = brightness * (1f - sectorFraction * saturation);
		float hueOffset = brightness * (1f - (1f - sectorFraction) * saturation);

		// assign rgb values based on sector
		switch (hueSector % 6) {
			case 0 -> {
				red = brightness;
				green = hueOffset;
				blue = lowerBound;
			}
			case 1 -> {
				red = higherBound;
				green = brightness;
				blue = lowerBound;
			}
			case 2 -> {
				red = lowerBound;
				green = brightness;
				blue = hueOffset;
			}
			case 3 -> {
				red = lowerBound;
				green = higherBound;
				blue = brightness;
			}
			case 4 -> {
				red = hueOffset;
				green = lowerBound;
				blue = brightness;
			}
			case 5 -> {
				red = brightness;
				green = lowerBound;
				blue = higherBound;
			}
		}
		int r = Math.round(red * 255f);
		int g = Math.round(green * 255f);
		int b = Math.round(blue * 255f);
		return ColorRGB.fromRGBA(r, g, b, 255);
	}

	/**
	 * Converts a {@link Color} to its grayscale equivalent.
	 *
	 * @param color the {@link Color} to convert to grayscale
	 * @return the colour's grayscale equivalent
	 */
	public static @NotNull ColorRGB toGrayscale(@NotNull Color color) {
		// weighted average simulates human perception
		int gray = (int) (0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue());
		return ColorRGB.fromRGBA(gray, gray, gray, color.getAlpha());
	}

	/**
	 * Converts a {@link Color} to its sepiatone equivalent.
	 *
	 * @param color the {@link Color} to convert to sepiatone
	 * @return the colour's sepiatone equivalent
	 */
	public static @NotNull ColorRGB toSepia(@NotNull Color color) {
		// standard sepia formula
		int r = color.getRed();
		int g = color.getGreen();
		int b = color.getBlue();
		int sepiaRed = (int) (0.393 * r + 0.769 * g + 0.189 * b);
		int sepiaGreen = (int) (0.349 * r + 0.686 * g + 0.168 * b);
		int sepiaBlue = (int) (0.272 * r + 0.534 * g + 0.131 * b);
		sepiaRed = Math.min(255, sepiaRed);
		sepiaGreen = Math.min(255, sepiaGreen);
		sepiaBlue = Math.min(255, sepiaBlue);
		return ColorRGB.fromRGBA(sepiaRed, sepiaGreen, sepiaBlue, color.getAlpha());
	}

	/**
	 * Adjusts the temperature of a {@link Color} by changing the red and blue channel values.
	 *
	 * @param color the {@link Color} to adjust the temperature of
	 * @param amount the amount to adjust the temperature by (-255 - 255)
	 * @return the temperature-adjusted colour
	 */
	public static @NotNull ColorRGB adjustTemperature(@NotNull Color color, int amount) {
		// increasing red and decreasing blue 'warms' the color, opposite cools
		int r = color.getRed() + amount;
		int b = color.getBlue() - amount;
		r = Math2.fit(0, r, 255);
		b = Math2.fit(0, b, 255);
		return ColorRGB.fromRGBA(r, color.getGreen(), b, color.getAlpha());
	}

}
