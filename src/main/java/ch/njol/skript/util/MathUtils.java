package ch.njol.skript.util;

/**
 * Simple utility methods for Math
 */
public class MathUtils {

	/**
	 * Clamp a value between a min and a max
	 * <p>
	 * This isn't available in Java 17
	 * Can be removed later when Java 21 is required
	 * </p>
	 *
	 * @param value Value to clamp
	 * @param min   Min value for clamping
	 * @param max   Max value for clamping
	 * @return Value, modifier if outside of clamped values
	 */
	public static int clamp(final int value, final int min, final int max) {
		if (min > max)
			throw new IllegalArgumentException(min + " > " + max);
		return Math.max(min, Math.min(max, value));
	}

}
