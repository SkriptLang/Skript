package org.skriptlang.skript.variables;

import ch.njol.util.StringUtils;
import org.jetbrains.annotations.ApiStatus;

import java.util.Comparator;

/**
 * The comparator for comparing variable names.
 * This decides the order of every list, so changing it would change the order of existing variables.
 */
@ApiStatus.Internal
public final class VariableNameComparator implements Comparator<String> {

	/**
	 * The shared instance. This has no state, so there's no need for another one.
	 */
	public static final VariableNameComparator INSTANCE = new VariableNameComparator();

	private VariableNameComparator() {
	}

	@Override
	public int compare(String s1, String s2) {
		if (s1 == null)
			return s2 == null ? 0 : -1;

		if (s2 == null)
			return 1;

		int len1 = s1.length();
		int len2 = s2.length();

		// Fast path: assume both strings are pure positive integers without leading zeros.
		// This is the dominant case for list indices (e.g. {list::1} through {list::1000}).
		// For these, numeric order == length order, then lexicographic order.
		// This does cause an extra partial loop over non-integer strings, but ints are much more common and it'll fail-fast.
		char firstChar1 = len1 > 0 ? s1.charAt(0) : 0;
		char firstChar2 = len2 > 0 ? s2.charAt(0) : 0;
		if (firstChar1 >= '1' && firstChar1 <= '9' && firstChar2 >= '1' && firstChar2 <= '9') {
			int i = 1;
			// Check if the rest of the characters are digits as well
			while (i < len1 && isDigit(s1.charAt(i))) {
				i++;
			}
			if (i == len1) { // all of s1 are digits
				i = 1;
				// Check if the rest of the characters are digits as well
				while (i < len2 && isDigit(s2.charAt(i))) {
					i++;
				}
				if (i == len2) { // all of s2 are digits
					if (len1 != len2)
						return len1 - len2;
					return s1.compareTo(s2);
				}
			}
		}

		int i = 0;
		int j = 0;

		boolean lastNumberNegative = false;
		boolean afterDecimalPoint = false;
		while (i < len1 && j < len2) {
			char c1 = s1.charAt(i);
			char c2 = s2.charAt(j);

			if (isDigit(c1) && isDigit(c2)) {
				// Numbers/digits are treated differently from other characters.

				// The index after the last digit
				int i2 = StringUtils.findLastDigit(s1, i);
				int j2 = StringUtils.findLastDigit(s2, j);

				// Amount of leading zeroes
				int z1 = 0;
				int z2 = 0;

				// Skip leading zeroes (except for the last if all 0's)
				if (!afterDecimalPoint) {
					if (c1 == '0') {
						while (i < i2 - 1 && s1.charAt(i) == '0') {
							i++;
							z1++;
						}
					}
					if (c2 == '0') {
						while (j < j2 - 1 && s2.charAt(j) == '0') {
							j++;
							z2++;
						}
					}
				}
				// Keep in mind that c1 and c2 may not have the right value (e.g. s1.charAt(i)) for the rest of this block

				// If the number is prefixed by a '-', it should be treated as negative, thus inverting the order.
				// If the previous number was negative, and the only thing separating them was a '.',
				//  then this number should also be in inverted order.
				boolean previousNegative = lastNumberNegative;

				// i - z1 contains the first digit, so i - z1 - 1 may contain a `-` indicating this number is negative
				lastNumberNegative = i - z1 > 0 && s1.charAt(i - z1 - 1) == '-';
				int isPositive = (lastNumberNegative | previousNegative) ? -1 : 1;

				// Different length numbers (99 > 9)
				if (!afterDecimalPoint && i2 - i != j2 - j)
					return ((i2 - i) - (j2 - j)) * isPositive;

				// Iterate over the digits
				while (i < i2 && j < j2) {
					char d1 = s1.charAt(i);
					char d2 = s2.charAt(j);

					// If the digits differ, return a value dependent on the sign
					if (d1 != d2)
						return (d1 - d2) * isPositive;

					i++;
					j++;
				}

				// Different length numbers (1.99 > 1.9)
				if (afterDecimalPoint && i2 - i != j2 - j)
					return ((i2 - i) - (j2 - j)) * isPositive;

				// If the numbers are equal, but either has leading zeroes,
				//  more leading zeroes is a bigger number (01 > 1)
				// The original intention was for them to be smaller, but the author wrote it wrong so they're bigger :/
				// Best to just keep it like that than to change it and break existing variable orderings
				if (z1 != z2)
					return (z1 - z2) * isPositive;

				afterDecimalPoint = true;
			} else {
				// Normal characters
				if (c1 != c2)
					return c1 - c2;

				// Reset the last number flags if we're exiting a number.
				if (c1 != '.') {
					lastNumberNegative = false;
					afterDecimalPoint = false;
				}

				i++;
				j++;
			}
		}
		if (i < len1)
			return lastNumberNegative ? -1 : 1;
		if (j < len2)
			return lastNumberNegative ? 1 : -1;
		return 0;
	}

	/**
	 * Simple helper for the comparator and {@link VariableBranch#positiveInt}.
	 *
	 * @param c The character to check
	 * @return Whether {@code c} is an ASCII digit
	 */
	static boolean isDigit(char c) {
		return '0' <= c && c <= '9';
	}

}
