package org.skriptlang.skript.util;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.UnmodifiableView;
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
	private final Set<String> mapIndices = new HashSet<>();

	public IndexTrackingTreeMap() {
		super();
	}

	public IndexTrackingTreeMap(Comparator<? super String> comparator) {
		super(comparator);
	}

	@Override
	public V put(String key, V value) {
		V previous = super.put(key, value);

		if (previous != null && value == null) {
			handleRemove(key, previous);
			return previous;
		}

		if (previous == null && value != null && isInt(key)) {
			try {
				int index = Integer.parseInt(key);
				if (consecutive() && index > numericalIndices.size()) {
					numericalIndices.add(index);
				} else {
					int pos = Collections.binarySearch(numericalIndices, index);
					numericalIndices.add(-pos - 1, index);
				}
			} catch (NumberFormatException ignore) {}
		}

		if (value instanceof Map) {
			mapIndices.add(key);
		} else if (previous instanceof Map) {
			mapIndices.remove(key);
		}

		return previous;
	}

	/**
	 * Adds the given value under the first available positive integer key.
	 *
	 * @param value the value to add, cannot be null
	 */
	public void add(V value) {
		Preconditions.checkNotNull(value, "value");
		int index = nextOpenIndex();
		String key = String.valueOf(index);
		super.put(key, value);
		numericalIndices.add(index - 1, index);
		if (value instanceof Map)
			mapIndices.add(key);
	}

	@Override
	public V remove(Object key) {
		V value = super.remove(key);
		if (value != null && key instanceof String index)
			handleRemove(index, value);
		return value;
	}

	@Override
	public void clear() {
		super.clear();
		numericalIndices.clear();
		mapIndices.clear();
	}

	public boolean consecutive() {
		int size = numericalIndices.size();
		if (size == 0)
			return true;
		return numericalIndices.getLast() == size;
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

		if (consecutive())
			return numericalIndices.size() + 1;

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

	/**
	 * Returns an unmodifiable view of the tracked numerical indices.
	 *
	 * @return a collection of all tracked positive integer keys
	 */
	public @UnmodifiableView Collection<Integer> numericalIndices() {
		return Collections.unmodifiableCollection(numericalIndices);
	}

	/**
	 * Returns an unmodifiable view of the keys that map to other {@link Map} instances.
	 *
	 * @return a collection of all keys pointing to a map
	 */
	public @UnmodifiableView Collection<String> mapIndices() {
		return Collections.unmodifiableCollection(mapIndices);
	}

	private void handleRemove(String index, Object previous) {
		if (previous instanceof Map)
			mapIndices.remove(index);

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
