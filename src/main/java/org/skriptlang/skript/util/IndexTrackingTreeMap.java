package org.skriptlang.skript.util;

import org.jetbrains.annotations.VisibleForTesting;

import java.util.*;

/**
 * A {@link TreeMap} that supports automatically assigning the next available
 * positive integer key, represented as a string.
 *
 * <p>In addition to arbitrary string keys, this map can be used with
 * positive integer string keys such as {@code "1"}, {@code "2"}, and
 * {@code "3"}. The {@link #add(Object)} method inserts a value using the
 * next available integer key.</p>
 *
 * @param <V> the type of mapped values
 */
public class IndexTrackingTreeMap<V> extends TreeMap<String, V> {

	@VisibleForTesting
	final List<Integer> numericalIndices = new ArrayList<>();

	public IndexTrackingTreeMap() {
		super();
	}

	public IndexTrackingTreeMap(Comparator<? super String> comparator) {
		super(comparator);
	}

	@Override
	public V put(String key, V value) {
		V previous = super.put(key, value);
		if (previous == null && isInt(key)) {
			try {
				int index = Integer.parseInt(key);
				int pos = Collections.binarySearch(numericalIndices, index);
				numericalIndices.add(-pos - 1, index);
			} catch (NumberFormatException ignore) {}
		} else if (previous != null && value == null) {
			handleRemove(key);
		}
		return previous;
	}

	/**
	 * Adds the given value under the first available positive integer key.
	 *
	 * @param value the value to add
	 */
	public void add(V value) {
		int index = nextOpenIndex();
		super.put(String.valueOf(index), value);
		numericalIndices.add(index - 1, index);
	}

	@Override
	public V remove(Object key) {
		V value = super.remove(key);
		if (value != null && key instanceof String index)
			handleRemove(index);
		return value;
	}

	@Override
	public void clear() {
		super.clear();
		numericalIndices.clear();
	}

	/**
	 * Finds the first available positive integer index that is not currently
	 * used as a key in this map.
	 *
	 * <p>This method inspects tracked numeric keys and returns the smallest
	 * missing index, starting at {@code 1}.</p>
	 *
	 * @return the next available positive integer index
	 */
	public int nextOpenIndex() {
		int size = numericalIndices.size();
		if (size == 0)
			return 1;

		if (numericalIndices.getFirst() > 1)
			return 1;

		int left = 0;
		int right = size - 1;

		while (left <= right) {
			int midpoint = left + (right - left >>> 1);

			if (numericalIndices.get(midpoint) == midpoint + 1) {
				left = midpoint + 1;
			} else {
				right = midpoint - 1;
			}
		}

		return left + 1;
	}

	private void handleRemove(String index) {
		if (!isInt(index))
			return;
		int pos = Collections.binarySearch(numericalIndices, Integer.parseInt(index));
		if (pos >= 0)
			numericalIndices.remove(pos);
	}

	private boolean isInt(String string) {
		if (string == null || string.isBlank() || string.charAt(0) == '0') // Don't handle leading-zero integers
			return false;
		for (int i = 0; i < string.length(); i++) {
			if (!isDigit(string.charAt(i)))
				return false;
		}

		return true;
	}

	private boolean isDigit(int codepoint) {
		return codepoint >= '0' && codepoint <= '9';
	}

}
