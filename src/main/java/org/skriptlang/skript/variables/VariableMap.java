package org.skriptlang.skript.variables;

import ch.njol.skript.lang.Variable;
import ch.njol.util.StringUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A map specifically for tracking and storage variables.
 * Maps from {@link #concurrent()} are thread-safe. Maps from {@link #local()} are confined to one thread at a time.
 */
@ApiStatus.Internal
public final class VariableMap {

	/**
	 * The comparator for comparing variable names.
	 */
	public static final Comparator<String> VARIABLE_NAME_COMPARATOR = (s1, s2) -> {
		//<editor-fold desc="variable name comparator" defaultstate="collapsed">
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
		//</editor-fold>
	};

	private static boolean isDigit(char c) {
		return '0' <= c && c <= '9';
	}

	private static final String LIST_SUFFIX = Variable.SEPARATOR + "*";

	/**
	 * This controls whether to use threadsafe concurrent data structures, which are needed for global variables,
	 * or to use plain hashmaps and treemaps, which are faster for local variables.
	 */
	private final boolean concurrent;

	/**
	 * This is the main value store
	 */
	private final Map<String, Object> values;
	private final @Nullable ReentrantLock lock;

	@Contract(" -> new")
	public static @NotNull VariableMap concurrent() {
		return new VariableMap(true);
	}

	@Contract(" -> new")
	public static @NotNull VariableMap local() {
		return new VariableMap(false);
	}

	/**
	 * Creates a new variable map with the specified type.
	 *
	 * @param concurrent Whether to enable concurrent data structures for thread safety.
	 */
	private VariableMap(boolean concurrent) {
		this.concurrent = concurrent;
		if (this.concurrent) {
			lock = new ReentrantLock();
			values = new ConcurrentHashMap<>();
		} else {
			lock = null;
			values = new HashMap<>();
		}
	}

	/**
	 * Returns values as a {@link ConcurrentHashMap} in order to avoid the values callsites from being megamorphic,
	 * and instead so the JIT can optimize the concrete type calls.
	 * Should only be called if {@link #concurrent} is true.
	 *
	 * @return The values map with a concrete type of {@link ConcurrentHashMap}.
	 */
	private ConcurrentHashMap<String, Object> sharedValues() {
		assert concurrent;
		return (ConcurrentHashMap<String, Object>) values;
	}

	/**
	 * Returns values as a {@link HashMap} in order to avoid the values callsites from being megamorphic,
	 * and instead so the JIT can optimize the concrete type calls.
	 * Should only be called if {@link #concurrent} is false.
	 *
	 * @return The values map with a concrete type of {@link HashMap}.
	 */
	private HashMap<String, Object> localValues() {
		assert !concurrent;
		return (HashMap<String, Object>) values;
	}

	private @Nullable Object valueGet(String key) {
		return concurrent ? sharedValues().get(key) : localValues().get(key);
	}

	private @Nullable Object valuePut(String key, @NotNull Object value) {
		return concurrent ? sharedValues().put(key, value) : localValues().put(key, value);
	}

	private @Nullable Object valueRemove(String key) {
		return concurrent ? sharedValues().remove(key) : localValues().remove(key);
	}

	private void lock() {
		if (lock != null) {
			lock.lock();
		}
	}

	private void unlock() {
		if (lock != null) {
			lock.unlock();
		}
	}

	public @Nullable Object getVariable(String name) {
		if (name.endsWith(LIST_SUFFIX)) {
			// TODO:
			return null;
		}
		return valueGet(name);
	}

	public @Nullable Object setVariable(String name, @Nullable Object value) {
		if (name.endsWith(LIST_SUFFIX)) {
			if (value != null)
				throw new IllegalArgumentException("Cannot set list variable '" + name + "' to a single value (" + value + ").");
			// TODO:
			return null;
		}
		Object previous = value == null ? valueRemove(name) : valuePut(name, value);
		// if we are adding/removing a value here, then we need to reconcile
		if ((previous == null) != (value == null)) {
			// TODO:
		}
		return previous;
	}

	public long size() {
		return concurrent ? sharedValues().mappingCount() : localValues().size();
	}

	public boolean isEmpty() {
		return concurrent ? sharedValues().isEmpty() : localValues().isEmpty();
	}

}
