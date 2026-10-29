package org.skriptlang.skript.util;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
 * <p>The next available index can never exceed {@code size() + 1}, so only small indices
 * matter for finding it. This means we can use a {@link BitSet} and avoid over-allocating
 * even if the list contains very large indices.</p>
 *
 * @param <V> the type of mapped values
 */
public class IndexTrackingTreeMap<V> extends TreeMap<String, V> {

	/**
	 * The initial and minimum number of indices tracked.
	 */
	private static final int MIN_TRACKED = 64;

	/**
	 * The largest the tracking limit may grow to: every positive int, so every index can be tracked.
	 */
	private static final int MAX_TRACKED = Integer.MAX_VALUE;

	/**
	 * The keys that map to other maps. Created on the first such key, as most lists have none.
	 * Used for excluding sublists in size calculations.
	 */
	private @Nullable Set<String> mapIndices;

	/**
	 * Bit {@code i} is set when positive integer key {@code i} maps to a non-null value,
	 * for {@code 1 <= i <= trackedLimit}. Keys above the limit are not tracked.
	 * Created on the first tracked index, so lists keyed only by other strings or large numbers never need it.
	 */
	private @Nullable BitSet trackedIndices;
	private int trackedLimit = MIN_TRACKED;

	/**
	 * The smallest positive integer that is not a key. Every index below it is set,
	 * so it never exceeds {@code size() + 1}. The one exception is {@link Integer#MAX_VALUE}, which it stays at
	 * once every index up to it is in use.
	 */
	private int nextIndex = 1;

	/**
	 * Creates a map from a pre-sorted set of keys and values in O(n) time.
	 * They must be sorted in the order the provided comparator would sort them.
	 * Failure to sort will cause undefined behavior.
	 *
	 * @param comparator The comparator by which the key-values were sorted.
	 * @param keys       The keys to use, in sorted order.
	 * @param values     The values to use, in order corresponding to the keys.
	 * @param <V>        The super type of the values.
	 * @return A new {@link IndexTrackingTreeMap} with the provided keys and values inserted and tracked.
	 */
	public static <V> IndexTrackingTreeMap<V> ofSorted(Comparator<? super String> comparator, String[] keys, V[] values) {
		Preconditions.checkArgument(keys.length == values.length);
		return new IndexTrackingTreeMap<>(new SortedEntries<>(comparator, keys, values));
	}

	public IndexTrackingTreeMap() {
		super();
	}

	public IndexTrackingTreeMap(Comparator<? super String> comparator) {
		super(comparator);
	}

	private IndexTrackingTreeMap(SortedEntries<V> sortedMap) {
		super(sortedMap);
		trackAll(sortedMap.keys, sortedMap.values);
	}

	/**
	 * Adds all the keys/values to the index tracking fields at once.
	 * Only for use when the map is currently empty
	 *
	 * @param keys   The keys to track.
	 * @param values The values of those keys.
	 */
	private void trackAll(String[] keys, V[] values) {
		// set size appropriately
		long limit = MIN_TRACKED;
		while (limit <= keys.length)
			limit *= 2;
		trackedLimit = (int) Math.min(limit, MAX_TRACKED);
		// for each key, add it to tracked indices + mapIndices if needed
		for (int i = 0; i < keys.length; i++) {
			if (values[i] instanceof Map)
				addMapIndex(keys[i]);
			int index = parsePositiveInt(keys[i]);
			if (index > 0 && index <= trackedLimit) {
				if (trackedIndices == null)
					trackedIndices = new BitSet(trackedLimit + 1);
				trackedIndices.set(index);
			}
		}
		// setup nextIndex
		nextIndex = trackedIndices == null ? 1 : nextClearIndex(1);
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
		int index = nextOpenIndex();
		String key = String.valueOf(index);

		super.put(key, value);
		handleInsert(key, index, value);
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
		mapIndices = null;
		trackedIndices = null;
		trackedLimit = MIN_TRACKED;
		nextIndex = 1;
	}

	/**
	 * Finds the first available positive integer index that is not currently
	 * used as a key in this map.
	 *
	 * <p>This method inspects tracked numeric keys and returns the smallest
	 * missing index, starting at {@code 1}.</p>
	 *
	 * @return the next available positive integer index
	 * @throws IllegalStateException if every index up to {@link Integer#MAX_VALUE} is in use
	 */
	public int nextOpenIndex() {
		if (nextIndex == Integer.MAX_VALUE && get(String.valueOf(Integer.MAX_VALUE)) != null)
			throw new IllegalStateException("Every index up to " + Integer.MAX_VALUE + " is in use, so no more values can be added");
		return nextIndex;
	}

	/**
	 * Returns whether the positive integer keys of this map are exactly {@code 1} to {@code n}
	 * for some {@code n >= 1}, with no gaps and nothing above.
	 * <p>
	 * This scans every key, as indices above the first gap are not necessarily tracked.
	 *
	 * @return whether the numeric keys are consecutive from 1
	 */
	public boolean consecutive() {
		if (nextIndex == 1)
			return false; // no numeric keys at all
		if (nextIndex == size() + 1)
			return true; // the entries are exactly 1..nextIndex - 1
		// find any numeric entries > nextIndex
		for (Map.Entry<String, V> entry : entrySet()) {
			if (entry.getValue() != null && parsePositiveInt(entry.getKey()) > nextIndex)
				return false;
		}
		return true;
	}

	/**
	 * Returns an unmodifiable view of the keys that map to other {@link Map} instances.
	 *
	 * @return a collection of all keys pointing to a map
	 */
	public @UnmodifiableView Collection<String> mapIndices() {
		if (mapIndices == null)
			return Collections.emptySet();
		return Collections.unmodifiableCollection(mapIndices);
	}

	private void addMapIndex(String key) {
		if (mapIndices == null)
			mapIndices = new HashSet<>();
		mapIndices.add(key);
	}

	private void removeMapIndex(String key) {
		if (mapIndices != null)
			mapIndices.remove(key);
	}

	/**
	 * Handles bookkeeping when inserting a value into the map, allocating mapIndices and BitSet if needed.
	 * @param key The key inserted.
	 * @param index The integer value of key, or -1 if not an int.
	 * @param value The value inserted.
	 */
	private void handleInsert(String key, int index, V value) {
		if (value instanceof Map)
			addMapIndex(key);

		if (index < 0)
			return;

		if (index <= trackedLimit) {
			if (trackedIndices == null)
				trackedIndices = new BitSet(MIN_TRACKED + 1);
			trackedIndices.set(index);
		}

		if (index == nextIndex)
			advanceNextIndex();
	}

	/**
	 * Handles bookkeeping when  replacing a value in the map, dealing with mapIndices if needed.
	 * @param key The key replaced.
	 * @param previous The previous value.
	 * @param value The new value.
	 */
	private void handleReplace(String key, V previous, V value) {
		if (value instanceof Map) {
			addMapIndex(key);
		} else if (previous instanceof Map) {
			removeMapIndex(key);
		}
	}

	/**
	 * Handles bookkeeping when removing a value from the map, handling mapIndices and resizing BitSet if needed.
	 * @param key The key removed.
	 * @param previous The former value.
	 */
	private void handleRemove(String key, V previous) {
		if (previous instanceof Map)
			removeMapIndex(key);

		int index = parsePositiveInt(key);
		if (index >= 0) {
			if (index <= trackedLimit && trackedIndices != null)
				trackedIndices.clear(index);
			nextIndex = Math.min(nextIndex, index);
		}

		shrinkIfSparse();
	}

	/**
	 * Moves {@link #nextIndex} to the first index after it that is not a key,
	 * growing the tracked range whenever every tracked index is in use.
	 */
	private void advanceNextIndex() {
		nextIndex = nextClearIndex(nextIndex);
		while (nextIndex > trackedLimit) { // never true once the limit is MAX_TRACKED, as no int exceeds it
			grow();
			nextIndex = nextClearIndex(nextIndex);
		}
	}

	/**
	 * Returns the first index from {@code from} onwards that is not tracked as in use.
	 * If every index from {@code from} up to {@link Integer#MAX_VALUE} is in use, returns
	 * {@link Integer#MAX_VALUE}, which {@link #nextOpenIndex()} then reports as full.
	 * {@link BitSet#nextClearBit(int)} would overflow past it instead.
	 */
	private int nextClearIndex(int from) {
		assert trackedIndices != null;
		// the last bit is only set once the tracked range covers every int, so this is one cheap check otherwise
		if (trackedIndices.get(Integer.MAX_VALUE) && trackedIndices.previousClearBit(Integer.MAX_VALUE) < from)
			return Integer.MAX_VALUE;
		return trackedIndices.nextClearBit(from);
	}

	/**
	 * Grows the tracked range to the first power of two above {@code size()}, which always holds
	 * {@link #nextIndex}, so one call is enough. Keys inside the new range were not tracked until now,
	 * so they are picked up with one pass over the entries, skipped when every entry is already tracked.
	 * This only happens once every tracked index is in use, so the map holds at least
	 * {@link #trackedLimit} entries and the pass is paid for by the inserts that filled it.
	 */
	private void grow() {
		int oldLimit = trackedLimit;
		long doubled = 2L * oldLimit; // long, as doubling past 2^30 overflows an int
		while (doubled <= size())
			doubled *= 2;
		int newLimit = (int) Math.min(doubled, MAX_TRACKED);
		trackedLimit = newLimit;
		assert trackedIndices != null;
		if (trackedIndices.cardinality() == size())
			return; // every entry is a tracked index, so none lies in the new range
		// catch any prior indices that should now be tracked
		for (Map.Entry<String, V> entry : entrySet()) {
			if (entry.getValue() == null)
				continue;
			int index = parsePositiveInt(entry.getKey());
			if (index > oldLimit && index <= newLimit)
				trackedIndices.set(index);
		}
	}

	/**
	 * Shrinks the tracked range once the map holds fewer than an eighth of it, so a list that
	 * grew large and was then mostly emptied does not keep its peak-sized tracking. Growing at
	 * full and shrinking at an eighth keeps a list hovering around one size from resizing back
	 * and forth. Indices dropped from tracking are picked up again by {@link #grow()} if needed.
	 */
	private void shrinkIfSparse() {
		if (trackedLimit <= MIN_TRACKED || size() >= trackedLimit / 8)
			return;
		int newLimit = MIN_TRACKED;
		while (newLimit < size() * 2)
			newLimit *= 2;
		// nextIndex <= size() + 1 <= newLimit, so it stays within the tracked range
		// ofSorted sizes the range to the entry count, so it can exceed the minimum with no index ever tracked
		if (trackedIndices != null)
			trackedIndices = trackedIndices.get(0, newLimit + 1);
		trackedLimit = newLimit;
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
				value = Math.addExact(Math.multiplyExact(value, 10), c - '0');
			}
		} catch (ArithmeticException e) { // overflow
			return -1;
		}

		return value;
	}

	private boolean isDigit(int codepoint) {
		return codepoint >= '0' && codepoint <= '9';
	}

	/**
	 * Lightweight SortedMap view of the key/value arrays for {@link #ofSorted(Comparator, String[], Object[])}.
	 * Only implements what the buildFromSorted TreeMap impl needs, but since that's not fixed api we do have to be
	 * mindful of any changes in future java versions. It's been stable from 8 to 25 though.
	 *
	 * @param <V> The supertype of the values.
	 */
	private static final class SortedEntries<V> extends AbstractMap<String, V> implements SortedMap<String, V> {
		private final Comparator<? super String> comparator;
		private final String[] keys;
		private final V[] values;

		public SortedEntries(Comparator<? super String> comparator, String[] keys, V[] values) {
			this.comparator = comparator;
			this.keys = keys;
			this.values = values;
		}


		@Override
		public Comparator<? super String> comparator() {
			return comparator;
		}

		@Override
		public @NotNull Set<Entry<String, V>> entrySet() {
			return new AbstractSet<>() {
				@Override
				public int size() {
					return keys.length;
				}

				@Override
				public Iterator<Entry<String, V>> iterator() {
					return new Iterator<>() {
						int i = 0;

						public boolean hasNext() {
							return i < keys.length;
						}

						public Entry<String, V> next() {
							return new SimpleImmutableEntry<>(keys[i], values[i++]);
						}
					};
				}
			};
		}

		//<editor-fold desc="unimplemented Set methods" defaultstate="collapsed">
		@Override
		public @NotNull SortedMap<String, V> subMap(String fromKey, String toKey) {
			throw new UnsupportedOperationException("SortedEntries had unimpl method called!");
		}

		@Override
		public @NotNull SortedMap<String, V> headMap(String toKey) {
			throw new UnsupportedOperationException("SortedEntries had unimpl method called!");
		}

		@Override
		public @NotNull SortedMap<String, V> tailMap(String fromKey) {
			throw new UnsupportedOperationException("SortedEntries had unimpl method called!");
		}

		@Override
		public String firstKey() {
			throw new UnsupportedOperationException("SortedEntries had unimpl method called!");
		}

		@Override
		public String lastKey() {
			throw new UnsupportedOperationException("SortedEntries had unimpl method called!");
		}
		//</editor-fold>
	}

}
