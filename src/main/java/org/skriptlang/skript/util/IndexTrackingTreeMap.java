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

	private final NavigableSet<Integer> freeIndices = new TreeSet<>();
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

		if (previous == null && value != null) {
			handleInsert(key, value);
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
		int index = freeIndices.removeFirst();
		String key = String.valueOf(index);

		super.put(key, value);

		if (freeIndices.isEmpty())
			freeIndices.add(index + 1);

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
		freeIndices.clear();
		freeIndices.add(1);
		mapIndices.clear();
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
		return freeIndices.first();
	}

	/**
	 * Returns an unmodifiable view of the keys that map to other {@link Map} instances.
	 *
	 * @return a collection of all keys pointing to a map
	 */
	public @UnmodifiableView Collection<String> mapIndices() {
		return Collections.unmodifiableCollection(mapIndices);
	}

	public void handleInsert(String key, V value) {
		if (value instanceof Map)
			mapIndices.add(key);

		int index = parsePositiveInt(key);
		if (index < 0)
			return;

		freeIndices.remove(index);

		if (!containsKey(String.valueOf(index + 1)))
			freeIndices.add(index + 1);
	}

	public void handleReplace(String key, V previous, V value) {
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

		freeIndices.add(Integer.parseInt(key));
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
