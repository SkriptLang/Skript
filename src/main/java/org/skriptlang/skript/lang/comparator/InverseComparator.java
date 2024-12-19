package org.skriptlang.skript.lang.comparator;

/**
 * Similar to {@link Comparator}, but {@link Comparator#compare(Object, Object)} arguments are switched.
 * If necessary, the resulting {@link Relation} is switched.
 *
 * @param <T1> The first type for comparison.
 * @param <T2> The second type for comparison.
 */
final class InverseComparator<T1, T2> implements Comparator<T1, T2> {

	private final ComparatorInfo<T2, T1> comparator;

	InverseComparator(ComparatorInfo<T2, T1> comparator) {
		this.comparator = comparator;
	}

	@Override
	public Relation compare(T1 o1, T2 o2) {
		return comparator.getComparator().compare(o2, o1).getSwitched();
	}

	@Override
	public boolean supportsOrdering() {
		return comparator.getComparator().supportsOrdering();
	}

	@Override
	public boolean supportsInversion() {
		return false;
	}

	@Override
	public String toString() {
		return "InverseComparator{comparator=" + comparator + "}";
	}

}
