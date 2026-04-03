package org.skriptlang.skript.util;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.UnmodifiableView;

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

	private final Set<String> mapIndices = new HashSet<>();

	private final Set<Integer> numericalIndices = new HashSet<>();
	private int nextIndex = 1;
	private int maxIndex = -1;

	public IndexTrackingTreeMap() {
		super();
	}

	public IndexTrackingTreeMap(Comparator<? super String> comparator) {
		super(comparator);
	}

	@Override
	public V put(String key, V value) {
		V previous = super.put(key, value);

		if (previous == null && value != null) {
			handleInsert(key, parsePositiveInt(key), value);
		} else if (previous != null && value == null) {
			handleRemove(key, previous);
		} else if (previous != null) {
			handleReplace(key, previous, value);
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
		String key = String.valueOf(nextIndex);

		super.put(key, value);
		handleInsert(key, nextIndex, value);
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
		nextIndex = 1;
		maxIndex = -1;
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
		return nextIndex;
	}

	public boolean consecutive() {
		return nextIndex == maxIndex + 1;
	}

	/**
	 * Returns an unmodifiable view of the keys that map to other {@link Map} instances.
	 *
	 * @return a collection of all keys pointing to a map
	 */
	public @UnmodifiableView Collection<String> mapIndices() {
		return Collections.unmodifiableCollection(mapIndices);
	}

	private void handleInsert(String key, int index, V value) {
		if (value instanceof Map)
			mapIndices.add(key);

		if (index < 0)
			return;

		numericalIndices.add(index);

		maxIndex = Math.max(maxIndex, index);
		advanceNextIndex();
	}

	private void handleReplace(String key, V previous, V value) {
		if (value instanceof Map) {
			mapIndices.add(key);
		} else if (previous instanceof Map) {
			mapIndices.remove(key);
		}
	}

	private void handleRemove(String key, V previous) {
		if (previous instanceof Map)
			mapIndices.remove(key);

		int index = parsePositiveInt(key);
		if (index < 0)
			return;

		numericalIndices.remove(index);

		if (index == maxIndex)
			recomputeMaxIndex();
		nextIndex = Math.min(nextIndex, index);
	}

	private void advanceNextIndex() {
		if (nextIndex == maxIndex) {
			nextIndex++;
			return;
		}
		while (numericalIndices.contains(nextIndex))
			nextIndex++;
	}

	private void recomputeMaxIndex() {
		while (maxIndex >= 0 && !numericalIndices.contains(maxIndex))
			maxIndex--;
	}

	private int parsePositiveInt(String string) {
		if (string == null || string.isBlank() || string.charAt(0) == '0') // Don't handle leading-zero integers
			return -1;

		int value = 0;
		try {
			for (int i = 0; i < string.length(); i++) {
				char c = string.charAt(i);
				if (!isDigit(c))
					return -1;
				value = Math.addExact(value * 10, c - '0');
			}
		} catch (ArithmeticException e) { // overflow
			return -1;
		}

		return value;
	}

	private boolean isDigit(int codepoint) {
		return codepoint >= '0' && codepoint <= '9';
	}

}
