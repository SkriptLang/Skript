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

import ch.njol.skript.Skript;

public abstract class Math2 {
	
	public static int min(int a, int b, int c) {
		return a <= b ? (a <= c ? a : c) : (b <= c ? b : c);
	}
	
	public static int min(int... nums) {
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
	
	public static int max(int a, int b, int c) {
		return a >= b ? (a >= c ? a : c) : (b >= c ? b : c);
	}
	
	public static int max(int... nums) {
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
	
	public static double min(double a, double b, double c) {
		return a <= b ? (a <= c ? a : c) : (b <= c ? b : c);
	}
	
	public static double min(double... nums) {
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
	
	public static double max(double a, double b, double c) {
		return a >= b ? (a >= c ? a : c) : (b >= c ? b : c);
	}
	
	public static double max(double... nums) {
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
	 * Fits a number into the given interval. The method's behaviour when min > max is unspecified.
	 * 
	 * @return <tt>x <= min ? min : x >= max ? max : x</tt>
	 */
	public static int fit(int min, int x, int max) {
		assert min <= max : min + "," + x + "," + max;
		return x <= min ? min : x >= max ? max : x;
	}

	/**
	 * Fits a number into the given interval. The method's behaviour when min > max is unspecified.
	 * 
	 * @return <tt>x <= min ? min : x >= max ? max : x</tt>
	 */
	public static float fit(float min, float x, float max) {
		assert min <= max : min + "," + x + "," + max;
		return x <= min ? min : x >= max ? max : x;
	}
	
	/**
	 * Fits a number into the given interval. The method's behaviour when min > max is unspecified.
	 * 
	 * @return <tt>x <= min ? min : x >= max ? max : x</tt>
	 */
	public static double fit(double min, double x, double max) {
		assert min <= max : min + "," + x + "," + max;
		return x <= min ? min : x >= max ? max : x;
	}
	
	/**
	 * Modulo that returns positive values even for negative arguments.
	 *
	 * @return <tt>d%m < 0 ? d%m + m : d%m</tt>
	 */
	public static double mod(double d, double m) {
		double r = d % m;
		return r < 0 ? r + m : r;
	}
	
	/**
	 * Modulo that returns positive values even for negative arguments.
	 *
	 * @return <tt>d%m < 0 ? d%m + m : d%m</tt>
	 */
	public static float mod(float d, float m) {
		float r = d % m;
		return r < 0 ? r + m : r;
	}
	
	/**
	 * Modulo that returns positive values even for negative arguments.
	 *
	 * @return <tt>d%m < 0 ? d%m + m : d%m</tt>
	 */
	public static int mod(int d, int m) {
		int r = d % m;
		return r < 0 ? r + m : r % m;
	}
	
	/**
	 * Modulo that returns positive values even for negative arguments.
	 *
	 * @return <tt>d%m < 0 ? d%m + m : d%m</tt>
	 */
	public static long mod(long d, long m) {
		long r = d % m;
		return r < 0 ? r + m : r % m;
	}
	
	/**
	 * Floors the given double and returns the result as a long.
	 * <p>
	 * This method can be up to 20 times faster than the default {@link Math#floor(double)} (both with and without casting to long).
	 */
	public static long floor(double d) {
		d += Skript.EPSILON;
		long l = (long) d;
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
	public static long ceil(double d) {
		d -= Skript.EPSILON;
		long l = (long) d;
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
	public static long round(double d) {
		d += Skript.EPSILON;
		if (Math.getExponent(d) >= 52)
			return (long) d;
		return floor(d + 0.5);
	}
	
	public static int floorI(double d) {
		d += Skript.EPSILON;
		int i = (int) d;
		if (!(d < 0)) // d >= 0 || d == NaN
			return i;
		if (i == Integer.MIN_VALUE)
			return Integer.MIN_VALUE;
		return d == i ? i : i - 1;
	}
	
	public static int ceilI(double d) {
		d -= Skript.EPSILON;
		int i = (int) d;
		if (!(d > 0)) // d <= 0 || d == NaN
			return i;
		if (i == Integer.MAX_VALUE)
			return Integer.MAX_VALUE;
		return d == i ? i : i + 1;
	}
	
	public static long floor(float f) {
		f += Skript.EPSILON;
		long l = (long) f;
		if (!(f < 0)) // f >= 0 || f == NaN
			return l;
		if (l == Long.MIN_VALUE)
			return Long.MIN_VALUE;
		return f == l ? l : l - 1;
	}

	/**
	 * Guarantees a float is neither NaN nor INF.
	 * Useful for situations when safe floats are required.
	 *
	 * @return 0 if f is NaN or INF, otherwise f
	 */
	public static float safe(float f) {
		if (f != f || Float.isInfinite(f)) //NaN or INF 
			return 0;
		return f;
	}

}
