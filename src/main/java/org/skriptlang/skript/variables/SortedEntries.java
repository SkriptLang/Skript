package org.skriptlang.skript.variables;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

/**
 * Lightweight SortedMap view of key/value arrays, for building sorted maps from sorted data in O(n) time.
 * The keys must already be sorted in the order of the comparator, with no repeats.
 * Only implements what the buildFromSorted TreeMap and ConcurrentSkipListMap impls need, but since that's not fixed
 * api we do have to be mindful of any changes in future java versions. It's been stable from 8 to 25 though.
 *
 * @param <V> The supertype of the values.
 */
@ApiStatus.Internal
public final class SortedEntries<V> extends AbstractMap<String, V> implements SortedMap<String, V> {
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
	public @NotNull Set<Map.Entry<String, V>> entrySet() {
		return new AbstractSet<>() {
			@Override
			public int size() {
				return keys.length;
			}

			@Override
			public Iterator<Map.Entry<String, V>> iterator() {
				return new Iterator<>() {
					int i = 0;

					public boolean hasNext() {
						return i < keys.length;
					}

					public Map.Entry<String, V> next() {
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
