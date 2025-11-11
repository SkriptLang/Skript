package ch.njol.util;

import ch.njol.skript.Skript;
import org.jetbrains.annotations.ApiStatus;

/**
 * This class is not to be used by addons. In the future methods may
 * change signature, contract and/or get removed without warning.
 * <p>
 * Behaviour for an edge case like NaN or infinite is undefined.
 */
@ApiStatus.Internal
public final class Math2 {

	private Math2() {}

	/**
	 * Fits an int into the given interval. The method's behaviour when min > max is unspecified.
	 *
	 * @return An int in between min and max
	 */
	public static int fit(int min, int value, int max) {
		assert min <= max : min + "," + value + "," + max;
		return Math.min(Math.max(value, min), max);
	}

	/**
	 * Fits a long into the given interval. The method's behaviour when min > max is unspecified.
	 *
	 * @return A long in between min and max
	 */
	public static long fit(long min, long value, long max) {
		assert min <= max : min + "," + value + "," + max;
		return Math.min(Math.max(value, min), max);
	}

	/**
	 * Fits a float into the given interval. The method's behaviour when min > max is unspecified.
	 *
	 * @return A float in between min and max
	 */
	public static float fit(float min, float value, float max) {
		assert min <= max : min + "," + value + "," + max;
		return Math.min(Math.max(value, min), max);
	}

	/**
	 * Fits a double into the given interval. The method's behaviour when min > max is unspecified.
	 *
	 * @return A double in between min and max
	 */
	public static double fit(double min, double value, double max) {
		assert min <= max : min + "," + value + "," + max;
		return Math.min(Math.max(value, min), max);
	}

	/**
	 * Fits the sum of {@code base} and {@code add} into the interval of {@code min} and {@link Integer#MAX_VALUE}.
	 * If the sum of {@code base} and {@code add} results in an overflow, returns {@link Integer#MAX_VALUE}.
	 *
	 * @return An int in between {@code min} and {@link Integer#MAX_VALUE} or {@link Integer#MAX_VALUE} if the sum overflowed.
	 */
	public static int fitOverflowMax(int min, int base, int add) {
		return fitOverflow(min, base, add, Integer.MAX_VALUE, Integer.MAX_VALUE);
	}

	/**
	 * Fits the sum of {@code base} and {@code add} into the interval of {@code min} and {@code max}.
	 * If the sum of {@code base} and {@code add} results in an overflow, returns {@code overflow}.
	 *
	 * @return An int in between {@code min} and {@code max} or {@code overflow} if the sum overflowed.
	 */
	public static int fitOverflow(int min, int base, int add, int max, int overflow) {
		assert min <= max;
		int value = base + add;
		boolean overflowed = value - base != add;
		if (overflowed)
			return overflow;
		return Math.min(Math.max(value, min), max);
	}

	/**
	 * Fits the sum of {@code base} and {@code add} into the interval of {@code min} and {@link Float#MAX_VALUE}.
	 * If the sum of {@code base} and {@code add} results in an overflow, returns {@link Float#MAX_VALUE}.
	 *
	 * @return A float in between {@code min} and {@link Float#MAX_VALUE} or {@link Float#MAX_VALUE} if the sum overflowed.
	 */
	public static float fitOverflowMax(float min, float base, float add) {
		return fitOverflow(min, base, add, Float.MAX_VALUE, Float.MAX_VALUE);
	}

	/**
	 * Fits the sum of {@code base} and {@code add} into the interval of {@code min} and {@code max}.
	 * If the sum of {@code base} and {@code add} results in an overflow, returns {@code overflow}.
	 *
	 * @return A float in between {@code min} and {@code max} or {@code overflow} if the sum overflowed.
	 */
	public static float fitOverflow(float min, float base, float add, float max, float overflow) {
		assert min <= max;
		float value = base + add;
		boolean overflowed = value - base != add;
		if (overflowed)
			return overflow;
		return Math.min(Math.max(value, min), max);
	}

	/**
	 * Modulo that returns positive values even for negative arguments.
	 *
	 * @return Int result of value modulo mod
	 */
	public static int mod(int value, int mod) {
		return (value % mod + mod) % mod;
	}

	/**
	 * Modulo that returns positive values even for negative arguments.
	 *
	 * @return Long result of value modulo mod
	 */
	public static long mod(long value, long mod) {
		return (value % mod + mod) % mod;
	}

	/**
	 * Modulo that returns positive values even for negative arguments.
	 *
	 * @return Float result of value modulo mod
	 */
	public static float mod(float value, float mod) {
		return (value % mod + mod) % mod;
	}

	/**
	 * Modulo that returns positive values even for negative arguments.
	 *
	 * @return Double result of value modulo mod
	 */
	public static double mod(double value, double mod) {
		return (value % mod + mod) % mod;
	}

	/**
	 * Ceils the given float and returns the result as an int.
	 */
	public static int ceil(float value) {
		return (int) Math.ceil(value - Skript.EPSILON);
	}

	/**
	 * Rounds the given float (where .5 is rounded up) and returns the result as an int.
	 */
	public static int round(float value) {
		return (int) Math.round(value + Skript.EPSILON);
	}

	/**
	 * Floors the given double and returns the result as a long.
	 */
	public static long floor(double value) {
		return (long) Math.floor(value + Skript.EPSILON);
	}

	/**
	 * Ceils the given double and returns the result as a long.
	 */
	public static long ceil(double value) {
		return (long) Math.ceil(value - Skript.EPSILON);
	}

	/**
	 * Rounds the given double (where .5 is rounded up) and returns the result as a long.
	 */
	public static long round(double value) {
		return Math.round(value + Skript.EPSILON);
	}

	/**
	 * Guarantees a float is neither NaN nor infinite.
	 * Useful for situations when safe floats are required.
	 *
	 * @return 0 if value is NaN or infinite, otherwise value
	 */
	public static float safe(float value) {
		return Float.isFinite(value) ? value : 0;
	}

	/**
	 * @param x the first value
	 * @param y the second value
	 * @return the sum of x and y, or {@link Long#MAX_VALUE} in case of an overflow
	 */
	public static long addClamped(long x, long y) {
		long result = x + y;
		// Logic extracted from Math#addExact to avoid having to catch an expensive exception
		boolean causedOverflow = ((x ^ result) & (y ^ result)) < 0;
		if (causedOverflow)
			return Long.MAX_VALUE;
		return result;
	}

	public static long multiplyClamped(long x, long y) {
		long result = x * y;
		long ax = Math.abs(x);
		long ay = Math.abs(y);
		// Logic extracted from Math#multiplyExact to avoid having to catch an expensive exception
		if (((ax | ay) >>> 31 != 0) && (((y != 0) && (result / y != x)) || (x == Long.MIN_VALUE && y == -1)))
			// If either x or y is negative return the min value, otherwise return the max value
			return x < 0 == y < 0 ? Long.MAX_VALUE : Long.MIN_VALUE;
		return result;
	}

}
