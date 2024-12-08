package org.skriptlang.skript.misc.colours;

import ch.njol.skript.util.Color;
import ch.njol.skript.util.ColorRGB;

/**
 * Utility class for colour manipulation and conversion.
 */
public class ColourUtils {

	/**
	 * Converts a {@link Color} to its hexadecimal representation.
	 *
	 * @param color the {@link Color} to convert
	 * @return the hexadecimal string of the colour
	 */
	public static String toHex(Color color) {
		return String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
	}

	/**
	 * Converts a {@link Color} to HSL (hue, saturation, lightness).
	 *
	 * @param color the {@link Color} to convert
	 * @return a float array representing the HSL values
	 */
	public static float[] rgbToHsl(Color color) {
		float r = color.getRed() / 255f;
		float g = color.getGreen() / 255f;
		float b = color.getBlue() / 255f;
		float max = Math.max(r, Math.max(g, b));
		float min = Math.min(r, Math.min(g, b));
		float h, s, l = (max + min) / 2f;
		if (max == min) {
			h = s = 0f;
		} else {
			float delta = max - min;
			s = l > 0.5f ? delta / (2f - max - min) : delta / (max + min);
			if (max == r) {
				h = ((g - b) / delta + (g < b ? 6f : 0f)) / 6f;
			} else if (max == g) {
				h = ((b - r) / delta + 2f) / 6f;
			} else {
				h = ((r - g) / delta + 4f) / 6f;
			}
		}
		return new float[]{ h, s, l };
	}

	/**
	 * Converts HSL values to a {@link ColorRGB} object.
	 *
	 * @param hsl a float array representing HSL values
	 * @return a {@link ColorRGB} object given the HSL values
	 */
	public static ColorRGB hslToRgb(float[] hsl) {
		float h = hsl[0], s = hsl[1], l = hsl[2];
		float r, g, b;
		if (s == 0f) {
			r = g = b = l;
		} else {
			float q = l < 0.5f ? l * (1f + s) : l + s - l * s;
			float p = 2f * l - q;
			r = hueToRgb(p, q, h + 1f / 3f);
			g = hueToRgb(p, q, h);
			b = hueToRgb(p, q, h - 1f / 3f);
		}
		int red = Math.round(r * 255f);
		int green = Math.round(g * 255f);
		int blue = Math.round(b * 255f);
		return ColorRGB.fromRGBA(red, green, blue, 255);
	}

	/**
	 * Helper method to convert hue values to RGB.
	 *
	 * @param p intermediate value
	 * @param q intermediate value
	 * @param t hue offset
	 * @return the calculated RGB value
	 */
	private static float hueToRgb(float p, float q, float t) {
		if (t < 0f)
			t += 1f;
		if (t > 1f)
			t -= 1f;
		if (t < 1f / 6f)
			return p + (q - p) * 6f * t;
		if (t < 1f / 2f)
			return q;
		if (t < 2f / 3f)
			return p + (q - p) * (2f / 3f - t) * 6f;
		return p;
	}

	/**
	 * Blends two {@link Color}s based on an amount from 0 to 100.
	 *
	 * @param c1 the first {@link Color}
	 * @param c2 the second {@link Color}
	 * @param amount the percentage amount to blend the colours (0 - 100)
	 * @return the blended colour
	 */
	public static Color blendColors(Color c1, Color c2, double amount) {
		amount = Math.max(0, Math.min(100, amount));
		amount /= 100.0;
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
	public static Color complementColor(Color color) {
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
	public static Color complementColorHSL(Color color) {
		float[] hsl = rgbToHsl(color);
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
	public static ColorRGB shadeColor(Color color, int amount) {
		amount = Math.max(1, Math.min(100, amount));
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
	public static ColorRGB shadeColorHSL(Color color, int amount) {
		amount = Math.max(1, Math.min(100, amount));
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
	public static ColorRGB tintColor(Color color, int amount) {
		amount = Math.max(1, Math.min(100, amount));
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
	public static ColorRGB tintColorHSL(Color color, int amount) {
		amount = Math.max(1, Math.min(100, amount));
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
	public static Color rotateHue(Color color, int degrees) {
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
	public static ColorRGB adjustBrightness(Color color, int amount) {
		amount = Math.max(-100, Math.min(100, amount));
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
	private static float[] rgbToHsb(Color color) {
		float r = color.getRed() / 255f;
		float g = color.getGreen() / 255f;
		float b = color.getBlue() / 255f;
		float max = Math.max(r, Math.max(g, b));
		float min = Math.min(r, Math.min(g, b));
		float delta = max - min;
		float h = 0f;
		float s = max == 0f ? 0f : delta / max;
		float v = max;
		if (delta != 0f) {
			if (max == r) {
				h = ((g - b) / delta) % 6f;
			} else if (max == g) {
				h = ((b - r) / delta) + 2f;
			} else {
				h = ((r - g) / delta) + 4f;
			}
			h *= 60f;
			if (h < 0f) h += 360f;
		}
		h /= 360f;
		return new float[]{ h, s, v };
	}

	/**
	 * Converts HSB values to a {@link ColorRGB} object.
	 * This is different to {@link #hslToRgb(float[])}.
	 *
	 * @param hsb a float array representing HSB values
	 * @return a {@link ColorRGB} object given the HSB values
	 */
	private static ColorRGB hsbToRgb(float[] hsb) {
		float h = hsb[0], s = hsb[1], v = hsb[2];
		float r = 0f, g = 0f, b = 0f;
		int i = (int) (h * 6f);
		float f = h * 6f - i;
		float p = v * (1f - s);
		float q = v * (1f - f * s);
		float t = v * (1f - (1f - f) * s);
		switch (i % 6) {
			case 0:
				r = v; g = t; b = p; break;
			case 1:
				r = q; g = v; b = p; break;
			case 2:
				r = p; g = v; b = t; break;
			case 3:
				r = p; g = q; b = v; break;
			case 4:
				r = t; g = p; b = v; break;
			case 5:
				r = v; g = p; b = q; break;
		}
		int red = Math.round(r * 255f);
		int green = Math.round(g * 255f);
		int blue = Math.round(b * 255f);
		return ColorRGB.fromRGBA(red, green, blue, 255);
	}

	/**
	 * Converts a {@link Color} to its grayscale equivalent.
	 *
	 * @param color the {@link Color} to convert to grayscale
	 * @return the colour's grayscale equivalent
	 */
	public static ColorRGB toGrayscale(Color color) {
		int gray = (int)(0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue());
		return ColorRGB.fromRGBA(gray, gray, gray, color.getAlpha());
	}

	/**
	 * Converts a {@link Color} to its sepiatone equivalent.
	 *
	 * @param color the {@link Color} to convert to sepiatone
	 * @return the colour's sepiatone equivalent
	 */
	public static ColorRGB toSepia(Color color) {
		int r = color.getRed();
		int g = color.getGreen();
		int b = color.getBlue();
		int tr = (int) (0.393 * r + 0.769 * g + 0.189 * b);
		int tg = (int) (0.349 * r + 0.686 * g + 0.168 * b);
		int tb = (int) (0.272 * r + 0.534 * g + 0.131 * b);
		tr = Math.min(255, tr);
		tg = Math.min(255, tg);
		tb = Math.min(255, tb);
		return ColorRGB.fromRGBA(tr, tg, tb, color.getAlpha());
	}

	/**
	 * Adjusts the temperature of a {@link Color} by changing the red and blue channel values.
	 *
	 * @param color the {@link Color} to adjust the temperature of
	 * @param amount the amount to adjust the temperature by (-255 - 255)
	 * @return the temperature-adjusted colour
	 */
	public static ColorRGB adjustTemperature(Color color, int amount) {
		int r = color.getRed() + amount;
		int b = color.getBlue() - amount;
		r = Math.max(0, Math.min(255, r));
		b = Math.max(0, Math.min(255, b));
		return ColorRGB.fromRGBA(r, color.getGreen(), b, color.getAlpha());
	}

}
