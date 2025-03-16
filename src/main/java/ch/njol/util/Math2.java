package ch.njol.util;

import ch.njol.skript.Skript;
import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval;
import org.jetbrains.annotations.ApiStatus;

import java.util.Arrays;

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

	@Deprecated
	@ScheduledForRemoval
	public static int floorI(double value) {
		return (int) Math.floor(value + Skript.EPSILON);
	}

	@Deprecated
	@ScheduledForRemoval
	public static int ceilI(double value) {
		return (int) Math.ceil(value - Skript.EPSILON);
	}

	// Change signature to return int instead of removing.
	@Deprecated
	@ScheduledForRemoval
	public static long floor(float value) {
		return (long) Math.floor(value + Skript.EPSILON);
	}

	@Deprecated
	@ScheduledForRemoval
	public static int min(int a, int b, int c) {
		return Math.min(a, Math.min(b, c));
	}

	@Deprecated
	@ScheduledForRemoval
	public static int min(int... numbers) {
		if (numbers.length == 0)
			return 0;

		return Arrays.stream(numbers)
			.min()
			.getAsInt();
	}

	@Deprecated
	@ScheduledForRemoval
	public static int max(int a, int b, int c) {
		return Math.max(a, Math.max(b, c));
	}

	@Deprecated
	@ScheduledForRemoval
	public static int max(int... numbers) {
		if (numbers.length == 0)
			return 0;

		return Arrays.stream(numbers)
			.max()
			.getAsInt();
	}

	@Deprecated
	@ScheduledForRemoval
	public static double min(double a, double b, double c) {
		return Math.min(a, Math.min(b, c));
	}

	@Deprecated
	@ScheduledForRemoval
	public static double min(double... numbers) {
		if (numbers.length == 0)
			return Double.NaN;

		return Arrays.stream(numbers)
			.min()
			.getAsDouble();
	}

	@Deprecated
	@ScheduledForRemoval
	public static double max(double a, double b, double c) {
		return Math.max(a, Math.max(b, c));
	}

	@Deprecated
	@ScheduledForRemoval
	public static double max(double... numbers) {
		if (numbers.length == 0)
			return Double.NaN;

		return Arrays.stream(numbers)
			.max()
			.getAsDouble();
	}

}
