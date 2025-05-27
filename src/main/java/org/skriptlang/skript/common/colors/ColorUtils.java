package org.skriptlang.skript.common.colors;

import ch.njol.skript.util.*;
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
			throw new IllegalArgumentException("Unsupported hex format - requires #RRGGBB or #AARRGGBB");
		}

		return ColorRGB.fromRGBA(red, green, blue, alpha);
	}

	/**
	 * Converts a {@link Color} to it's HSL (hue, saturation, lightness) representation.
	 *
	 * @param color the {@link Color} to convert
	 * @return an immutable {@link ColorHSL}
	 */
	public static @NotNull ColorHSL rgbToHsl(@NotNull Color color) {
		// normalize rgb values to between 0 and 1
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
		return ColorHSL.fromHSLA(hue, saturation, lightness, color.getAlpha());
	}

	/**
	 * Converts a {@link ColorHSL} object to it's {@link ColorRGB} equivalent.
	 *
	 * @param hsl the {@link ColorHSL} to convert
	 * @return a {@link ColorRGB} object representing the same color
	 */
	public static @NotNull ColorRGB hslToRgb(@NotNull ColorHSL hsl) {
		float hue = hsl.getHue();
		float saturation = hsl.getSaturation();
		float lightness = hsl.getLightness();

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
		return ColorRGB.fromRGBA(r, g, b, hsl.getAlpha());
	}

	/**
	 * Converts a {@link Color} to it's HSB (hue, saturation, brightness) representation.
	 *
	 * @param color the {@link Color} to convert
	 * @return an immutable {@link ColorHSB}
	 */
	public static @NotNull ColorHSB rgbToHsb(@NotNull Color color) {
		float red = color.getRed() / 255f;
		float green = color.getGreen() / 255f;
		float blue = color.getBlue() / 255f;

		float max = Math.max(red, Math.max(green, blue));
		float min = Math.min(red, Math.min(green, blue));
		float delta = max - min;

		float hue = 0f;
		if (delta != 0f) {
			if (max == red)
				hue = ((green - blue) / delta) % 6f;
			else if (max == green)
				hue = ((blue - red) / delta) + 2f;
			else
				hue = ((red - green) / delta) + 4f;

			hue /= 6f;
			if (hue < 0f)
				hue += 1f;
		}
		float saturation = max == 0f ? 0f : delta / max;
		return ColorHSB.fromHSBA(hue, saturation, max, color.getAlpha());
	}

	/**
	 * Converts a {@link ColorHSB} object to it's {@link ColorRGB} equivalent.
	 *
	 * @param hsb the {@link ColorHSB} to convert
	 * @return a {@link ColorRGB} object representing the same color
	 */
	public static @NotNull ColorRGB hsbToRgb(@NotNull ColorHSB hsb) {
		float hue = hsb.getHue();
		float saturation = hsb.getSaturation();
		float brightness = hsb.getBrightness();

		// determine hue sector and offset within said sector
		int sector = (int) (hue * 6f);
		float remainder = hue * 6f - sector;

		// compute three intermediate values
		float chromaLow = brightness * (1f - saturation);
		float chromeHighSlope = brightness * (1f - remainder * saturation);
		float chromaLowSlope = brightness * (1f - (1f - remainder) * saturation);

		float red = 0f, green = 0f, blue = 0f;
		// pick the permutation of brightness, chroma low slope, and chroma lwo based on sector
		switch (sector % 6) {
			case 0 -> {
				red = brightness;
				green = chromaLowSlope;
				blue = chromaLow;
			}
			case 1 -> {
				red = chromeHighSlope;
				green = brightness;
				blue = chromaLow;
			}
			case 2 -> {
				red = chromaLow;
				green = brightness;
				blue = chromaLowSlope;
			}
			case 3 -> {
				red = chromaLow;
				green = chromeHighSlope;
				blue = brightness;
			}
			case 4 -> {
				red = chromaLowSlope;
				green = chromaLow;
				blue = brightness;
			}
			case 5 -> {
				red = brightness;
				green = chromaLow;
				blue = chromeHighSlope;
			}
		}
		// scale to 0 - 255 range
		return ColorRGB.fromRGBA(Math.round(red * 255f), Math.round(green * 255f), Math.round(blue * 255f), hsb.getAlpha());
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
	 * @param amount the percentage amount to blend the colors (0 - 100)
	 * @return the blended color
	 */
	public static @NotNull Color blendColors(@NotNull Color c1, @NotNull Color c2, double amount) {
		// amount is a percentage (clamp then normalize to between 0 and 1)
		amount = Math2.fit(0, amount, 100) / 100.0;

		// linearly interpolate each channel
		int red = (int) (c1.getRed() * (1 - amount) + c2.getRed() * amount);
		int green = (int) (c1.getGreen() * (1 - amount) + c2.getGreen() * amount);
		int blue = (int) (c1.getBlue() * (1 - amount) + c2.getBlue() * amount);
		int alpha = (int) (c1.getAlpha() * (1 - amount) + c2.getAlpha() * amount);
		return ColorRGB.fromRGBA(red, green, blue, alpha);
	}

	/**
	 * Calculates the complement of a {@link Color}.
	 *
	 * @param color the {@link Color} to complement
	 * @return the complementary colour
	 */
	public static @NotNull Color complementColor(@NotNull Color color) {
		// just invert each channel
		int red = 255 - color.getRed();
		int green = 255 - color.getGreen();
		int blue = 255 - color.getBlue();
		return ColorRGB.fromRGBA(red, green, blue, color.getAlpha());
	}

	/**
	 * Calculates the complement of a {@link Color} using HSL adjustments.
	 *
	 * @param color the {@link Color} to complement
	 * @return the complementary colour
	 */
	public static @NotNull Color complementColorHSL(@NotNull Color color) {
		ColorHSL hsl = rgbToHsl(color);
		float newHue = (hsl.getHue() + 0.5f) % 1f; // 180 degree rotation
		ColorHSL complemented = ColorHSL.fromHSLA(newHue, hsl.getSaturation(), hsl.getLightness(), color.getAlpha());
		return hslToRgb(complemented);
	}

	/**
	 * Shades a {@link Color} by a given amount from 1 to 100.
	 *
	 * @param color the {@link Color} to shade
	 * @param amount the amount to shade the color by (1 - 100)
	 * @return the shaded color
	 */
	public static @NotNull ColorRGB shadeColor(@NotNull Color color, int amount) {
		// reducing the channel values darkens the color
		amount = Math2.fit(1, amount, 100);
		double factor = (100 - amount) / 100.0;
		int red = (int) (color.getRed() * factor);
		int green = (int) (color.getGreen() * factor);
		int blue = (int) (color.getBlue() * factor);
		return ColorRGB.fromRGBA(red, green, blue, color.getAlpha());
	}

	/**
	 * Shades a {@link Color} by a given amount from 1 to 100 using HSL adjustments.
	 *
	 * @param color the {@link Color} to shade using HSL adjustments
	 * @param amount the amount to shade the color by (1 - 100)
	 * @return the shaded color
	 */
	public static @NotNull ColorRGB shadeColorHSL(@NotNull Color color, int amount) {
		// reducing the lightness to shade
		amount = Math2.fit(1, amount, 100);
		ColorHSL hsl = rgbToHsl(color);
		float newLightness = hsl.getLightness() * (100 - amount) / 100f;
		ColorHSL shaded = ColorHSL.fromHSLA(hsl.getHue(), hsl.getSaturation(), newLightness, color.getAlpha());
		return hslToRgb(shaded);
	}

	/**
	 * Tints a {@link Color} by a given amount from 1 to 100.
	 *
	 * @param color the {@link Color} to tint
	 * @param amount the amount to tint the color by (1 - 100)
	 * @return the tinted color
	 */
	public static @NotNull ColorRGB tintColor(@NotNull Color color, int amount) {
		// move each channel closer to 255 to lighten the color
		amount = Math2.fit(1, amount, 100);
		double factor = amount / 100.0;
		int red = (int) (color.getRed() + (255 - color.getRed()) * factor);
		int green = (int) (color.getGreen() + (255 - color.getGreen()) * factor);
		int blue = (int) (color.getBlue() + (255 - color.getBlue()) * factor);
		return ColorRGB.fromRGBA(red, green, blue, color.getAlpha());
	}

	/**
	 * Tints a {@link Color} by a given amount from 1 to 100 using HSL adjustments.
	 *
	 * @param color the {@link Color} to tint using HSL adjustments
	 * @param amount the amount to tint the color by (1 - 100)
	 * @return the tinted color
	 */
	public static @NotNull ColorRGB tintColorHSL(@NotNull Color color, int amount) {
		// increasing the lightness (towards 1) to tint
		amount = Math2.fit(1, amount, 100);
		ColorHSL hsl = rgbToHsl(color);
		float newLightness = hsl.getLightness() + (1f - hsl.getLightness()) * (amount / 100f);
		newLightness = Math.min(1f, newLightness);
		ColorHSL tinted = ColorHSL.fromHSLA(hsl.getHue(), hsl.getSaturation(), newLightness, color.getAlpha());
		return hslToRgb(tinted);
	}

	/**
	 * Rotates the hue of a {@link Color} by a given degree.
	 *
	 * @param color the {@link Color} to rotate the hue of
	 * @param degrees the number of degrees to rotate the hue by
	 * @return the hue-rotated color
	 */
	public static @NotNull Color rotateHue(@NotNull Color color, int degrees) {
		// hue is a fraction of a circle, add (degrees/360) to rotate by that angle
		ColorHSL hsl = rgbToHsl(color);
		float newHue = (hsl.getHue() + degrees / 360f) % 1f;
		if (newHue < 0f)
			newHue += 1f;
		ColorHSL rotated = ColorHSL.fromHSLA(newHue, hsl.getSaturation(), hsl.getLightness(), color.getAlpha());
		return hslToRgb(rotated);
	}

	/**
	 * Adjusts the brightness of a {@link Color} by an amount from -100 to 100.
	 * This is similar to shading and tinting, but is slightly different.
	 *
	 * @param color the {@link Color} to adjust the brightness of
	 * @param amount the amount to adjust the brightness by (-100 - 100)
	 * @return the brightness-adjusted color
	 */
	public static @NotNull ColorRGB adjustBrightness(@NotNull Color color, int amount) {
		// adjust brightness by scaling brightness directly (shocking stuff)
		amount = Math2.fit(-100, amount, 100);
		ColorHSB hsb = rgbToHsb(color);
		float factor = amount / 100f;
		float newBrightness = hsb.getBrightness() + hsb.getBrightness() * factor;
		newBrightness = Math2.fit(0f, newBrightness, 1f);
		return hsbToRgb(ColorHSB.fromHSBA(hsb.getHue(), hsb.getSaturation(), newBrightness, color.getAlpha()));
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
		int red = color.getRed();
		int green = color.getGreen();
		int blue = color.getBlue();
		int sepiaRed = (int) (0.393 * red + 0.769 * green + 0.189 * blue);
		int sepiaGreen = (int) (0.349 * red + 0.686 * green + 0.168 * blue);
		int sepiaBlue = (int) (0.272 * red + 0.534 * green + 0.131 * blue);
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
	 * @return the temperature-adjusted color
	 */
	public static @NotNull ColorRGB adjustTemperature(@NotNull Color color, int amount) {
		// increasing red and decreasing blue 'warms' the color, opposite cools
		int red = color.getRed() + amount;
		int blue = color.getBlue() - amount;
		red = Math2.fit(0, red, 255);
		blue = Math2.fit(0, blue, 255);
		return ColorRGB.fromRGBA(red, color.getGreen(), blue, color.getAlpha());
	}

}
