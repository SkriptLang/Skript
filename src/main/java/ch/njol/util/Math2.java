/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.util;

/**
 * @author Peter Güttinger
 */
public abstract class Math2 {
	
	public static int min(final int a, final int b, final int c) {
		return a <= b ? (a <= c ? a : c) : (b <= c ? b : c);
	}
	
	public static int min(final int... nums) {
		if (nums == null || nums.length == 0) {
			assert false;
			return 0;
		}
		int min = nums[0];
		for (int i = 1; i < nums.length; i++) {
			if (nums[i] < min)
				min = nums[i];
		}
		return min;
	}
	
	public static int max(final int a, final int b, final int c) {
		return a >= b ? (a >= c ? a : c) : (b >= c ? b : c);
	}
	
	public static int max(final int... nums) {
		if (nums == null || nums.length == 0) {
			assert false;
			return 0;
		}
		int max = nums[0];
		for (int i = 1; i < nums.length; i++) {
			if (nums[i] > max)
				max = nums[i];
		}
		return max;
	}
	
	public static double min(final double a, final double b, final double c) {
		return a <= b ? (a <= c ? a : c) : (b <= c ? b : c);
	}
	
	public static double min(final double... nums) {
		if (nums == null || nums.length == 0) {
			assert false;
			return Double.NaN;
		}
		double min = nums[0];
		for (int i = 1; i < nums.length; i++) {
			if (nums[i] < min)
				min = nums[i];
		}
		return min;
	}
	
	public static double max(final double a, final double b, final double c) {
		return a >= b ? (a >= c ? a : c) : (b >= c ? b : c);
	}
	
	public static double max(final double... nums) {
		if (nums == null || nums.length == 0) {
			assert false;
			return Double.NaN;
		}
		double max = nums[0];
		for (int i = 1; i < nums.length; i++) {
			if (nums[i] > max)
				max = nums[i];
		}
		return max;
	}
	
	/**
	 * finds the smallest positive number (&ge;0) in the sequence
	 * 
	 * @param nums
	 * @return smallest positive number in the sequence or -1 if no number is positive
	 */
	public static int minPositive(final int... nums) {
		int max = -1;
		if (nums != null) {
			for (final int num : nums) {
				if (num >= 0 && (num < max || max == -1))
					max = num;
			}
		}
		return max;
	}
	
	/**
	 * Fits a number into the given interval. The method's behaviour when min > max is unspecified.
	 * 
	 * @return <tt>x <= min ? min : x >= max ? max : x</tt>
	 */
	public static int fit(final int min, final int x, final int max) {
		assert min <= max : min + "," + x + "," + max;
		return x <= min ? min : x >= max ? max : x;
	}
	
	/**
	 * Fits a number into the given interval. The method's behaviour when min > max is unspecified.
	 * 
	 * @return <tt>x <= min ? min : x >= max ? max : x</tt>
	 */
	public static short fit(final short min, final short x, final short max) {
		assert min <= max : min + "," + x + "," + max;
		return x <= min ? min : x >= max ? max : x;
	}
	
	/**
	 * Fits a number into the given interval. The method's behaviour when min > max is unspecified.
	 * 
	 * @return <tt>x <= min ? min : x >= max ? max : x</tt>
	 */
	public static long fit(final long min, final long x, final long max) {
		assert min <= max : min + "," + x + "," + max;
		return x <= min ? min : x >= max ? max : x;
	}
	
	/**
	 * Fits a number into the given interval. The method's behaviour when min > max is unspecified.
	 * 
	 * @return <tt>x <= min ? min : x >= max ? max : x</tt>
	 */
	public static float fit(final float min, final float x, final float max) {
		assert min <= max : min + "," + x + "," + max;
		return x <= min ? min : x >= max ? max : x;
	}
	
	/**
	 * Fits a number into the given interval. The method's behaviour when min > max is unspecified.
	 * 
	 * @return <tt>x <= min ? min : x >= max ? max : x</tt>
	 */
	public static double fit(final double min, final double x, final double max) {
		assert min <= max : min + "," + x + "," + max;
		return x <= min ? min : x >= max ? max : x;
	}
	
	/**
	 * Modulo that returns positive values even for negative arguments.
	 * 
	 * @param d
	 * @param m
	 * @return <tt>d%m < 0 ? d%m + m : d%m</tt>
	 */
	public static double mod(final double d, final double m) {
		final double r = d % m;
		return r < 0 ? r + m : r;
	}
	
	/**
	 * Modulo that returns positive values even for negative arguments.
	 * 
	 * @param d
	 * @param m
	 * @return <tt>d%m < 0 ? d%m + m : d%m</tt>
	 */
	public static float mod(final float d, final float m) {
		final float r = d % m;
		return r < 0 ? r + m : r;
	}
	
	/**
	 * Modulo that returns positive values even for negative arguments.
	 * 
	 * @param d
	 * @param m
	 * @return <tt>d%m < 0 ? d%m + m : d%m</tt>
	 */
	public static int mod(final int d, final int m) {
		final int r = d % m;
		return r < 0 ? r + m : r % m;
	}
	
	/**
	 * Modulo that returns positive values even for negative arguments.
	 * 
	 * @param d
	 * @param m
	 * @return <tt>d%m < 0 ? d%m + m : d%m</tt>
	 */
	public static long mod(final long d, final long m) {
		final long r = d % m;
		return r < 0 ? r + m : r % m;
	}
	
	/**
	 * Floors the given double and returns the result as a long.
	 * <p>
	 * This method can be up to 20 times faster than the default {@link Math#floor(double)} (both with and without casting to long).
	 */
	public static long floor(final double d) {
		final long l = (long) d;
		if (!(d < 0)) // d >= 0 || d == NaN
			return l;
		if (l == Long.MIN_VALUE)
			return Long.MIN_VALUE;
		return d == l ? l : l - 1;
	}
	
	/**
	 * Ceils the given double and returns the result as a long.
	 * <p>
	 * This method can be up to 20 times faster than the default {@link Math#ceil(double)} (both with and without casting to long).
	 */
	public static long ceil(final double d) {
		final long l = (long) d;
		if (!(d > 0)) // d <= 0 || d == NaN
			return l;
		if (l == Long.MAX_VALUE)
			return Long.MAX_VALUE;
		return d == l ? l : l + 1;
	}
	
	/**
	 * Rounds the given double (where .5 is rounded up) and returns the result as a long.
	 * <p>
	 * This method is more exact and faster than {@link Math#round(double)} of Java 7 and older.
	 */
	public static long round(final double d) {
		if (d == 0x1.fffffffffffffp-2) // greatest double value less than 0.5
			return 0;
		if (Math.getExponent(d) >= 52)
			return (long) d;
		return floor(d + 0.5);
	}
	
	public static int floorI(final double d) {
		final int i = (int) d;
		if (!(d < 0)) // d >= 0 || d == NaN
			return i;
		if (i == Integer.MIN_VALUE)
			return Integer.MIN_VALUE;
		return d == i ? i : i - 1;
	}
	
	public static int ceilI(final double d) {
		final int i = (int) d;
		if (!(d > 0)) // d <= 0 || d == NaN
			return i;
		if (i == Integer.MAX_VALUE)
			return Integer.MAX_VALUE;
		return d == i ? i : i + 1;
	}
	
	public static int roundI(final double d) {
		if (d == 0x1.fffffffffffffp-2) // greatest double value less than 0.5
			return 0;
		if (Math.getExponent(d) >= 52)
			return (int) d;
		return floorI(d + 0.5);
	}
	
	public static long floor(final float f) {
		final long l = (long) f;
		if (!(f < 0)) // f >= 0 || f == NaN
			return l;
		if (l == Long.MIN_VALUE)
			return Long.MIN_VALUE;
		return f == l ? l : l - 1;
	}
	
	public static long ceil(final float f) {
		final long l = (long) f;
		if (!(f > 0)) // f <= 0 || f == NaN
			return l;
		if (l == Long.MAX_VALUE)
			return Long.MAX_VALUE;
		return f == l ? l : l + 1;
	}
	
	/**
	 * Rounds the given float (where .5 is rounded up) and returns the result as a long.
	 * <p>
	 * This method is more exact and faster than {@link Math#round(float)} of Java 7 and older.
	 */
	public static long round(final float f) {
		if (f == 0x1.fffffep-2f) // greatest float value less than 0.5
			return 0;
		if (Math.getExponent(f) >= 23)
			return (long) f;
		return floor(f + 0.5f);
	}
	
	public static int floorI(final float f) {
		final int i = (int) f;
		if (!(f < 0)) // f >= 0 || f == NaN
			return i;
		if (i == Integer.MIN_VALUE)
			return Integer.MIN_VALUE;
		return f == i ? i : i - 1;
	}
	
	public static int ceilI(final float f) {
		final int i = (int) f;
		if (!(f > 0)) // f <= 0 || f == NaN
			return i;
		if (i == Integer.MAX_VALUE)
			return Integer.MAX_VALUE;
		return f == i ? i : i + 1;
	}
	
	/**
	 * Rounds the given float (where .5 is rounded up) and returns the result as an int.
	 * <p>
	 * This method is more exact and faster than {@link Math#round(float)} of Java 7 and older.
	 */
	public static int roundI(final float f) {
		if (f == 0x1.fffffep-2f) // greatest float value less than 0.5
			return 0;
		if (Math.getExponent(f) >= 23)
			return (int) f;
		return floorI(f + 0.5f);
	}
	
	/**
	 * Gets the smallest power of two &ge;n. Returns {@link Integer#MIN_VALUE} if <tt>n > 2<sup>30</sup></tt>.
	 */
	public static int nextPowerOfTwo(final int n) {
		if (n < 0) {
			int h = ~n;
			h |= (h >> 1);
			h |= (h >> 2);
			h |= (h >> 4);
			h |= (h >> 8);
			h |= (h >> 16);
			h = ~h;
			return n == h ? n : h >> 1;
		} else {
			final int h = Integer.highestOneBit(n);
			return n == h ? n : h << 1;
		}
	}
	
	/**
	 * Gets the smallest power of two &ge;n. Returns {@link Long#MIN_VALUE} if <tt>n > 2<sup>62</sup></tt>.
	 */
	public static long nextPowerOfTwo(final long n) {
		if (n < 0) {
			long h = ~n;
			h |= (h >> 1);
			h |= (h >> 2);
			h |= (h >> 4);
			h |= (h >> 8);
			h |= (h >> 16);
			h |= (h >> 32);
			h = ~h;
			return n == h ? n : h >> 1;
		} else {
			final long h = Long.highestOneBit(n);
			return n == h ? n : h << 1;
		}
	}
	
	/**
	 * @return The floating point part of d in the range [0, 1)
	 */
	public static double frac(final double d) {
		final double r = mod(d, 1);
		return r == 1 ? 0 : r;
	}
	
	/**
	 * @return The floating point part of f in the range [0, 1)
	 */
	public static float frac(final float f) {
		final float r = mod(f, 1);
		return r == 1 ? 0 : r;
	}
	
	/**
	 * @return -1 if i is negative, 0 if i is 0, or 1 if i is positive
	 */
	public static int sign(final byte i) {
		return (i >> 7) | (-i >>> 7);
	}
	
	/**
	 * @return -1 if i is negative, 0 if i is 0, or 1 if i is positive
	 */
	public static int sign(final short i) {
		return (i >> 15) | (-i >>> 15);
	}
	
	/**
	 * @return -1 if i is negative, 0 if i is 0, or 1 if i is positive
	 */
	public static int sign(final int i) {
		return (i >> 31) | (-i >>> 31);
	}
	
	/**
	 * @return -1 if i is negative, 0 if i is 0, or 1 if i is positive
	 */
	public static int sign(final long i) {
		return (int) (i >> 63) | (int) (-i >>> 63);
	}
	
	/**
	 * @return -1 if f is negative, 0 if f is +0, -0 or NaN, or 1 if f is positive
	 */
	public static int sign(final float f) {
		return f > 0 ? 1 : f < 0 ? -1 : 0;
	}
	
	/**
	 * @return -1 if d is negative, 0 if d is +0, -0 or NaN, or 1 if d is positive
	 */
	public static int sign(final double d) {
		return d > 0 ? 1 : d < 0 ? -1 : 0;
	}
	
	/**
	 * Performs a hermite interpolation between the given values, or returns 0 or 1 respectively if the value is out of range.
	 * <p>
	 * Specifically this method returns <tt>d * d * (3 - 2 * d)</tt>, where <tt>d = {@link #fit(double, double, double) fit}(0, (x - x1) / (x2 - x1), 1)</tt>. This is very similar
	 * to <tt>0.5 - 0.5 * cos(PI * d)</tt>.
	 * <p>
	 * This function is essentially equal to GLSL's smoothstep, but with a different argument order.
	 * 
	 * @param x The value to get the step at
	 * @param x1 The lower end of the step
	 * @param x2 The upper end of the step
	 * @return The step's value at <tt>x</tt>
	 */
	public static double smoothStep(final double x, final double x1, final double x2) {
		final double d = fit(0, (x - x1) / (x2 - x1), 1);
		return d * d * (3 - 2 * d);
	}
	
	/**
	 * Guarantees a float is neither NaN nor INF.
	 * Useful for situations when safe floats are required.
	 * 
	 * @param f
	 * @return 0 if f is NaN or INF, otherwise f
	 */
	public static float safe(float f) {
		if (f != f || Float.isInfinite(f)) //NaN or INF 
			return 0;
		return f;
	}
	
	/**
	 * Guarantees a double is neither NaN nor INF.
	 * Useful for situations when safe doubles are required.
	 * 
	 * @param d
	 * @return 0 if d is NaN or INF, otherwise d
	 */
	public static double safe(double d) {
		if (d != d || Double.isInfinite(d)) //NaN or INF 
			return 0;
		return d;
	}
	
}
