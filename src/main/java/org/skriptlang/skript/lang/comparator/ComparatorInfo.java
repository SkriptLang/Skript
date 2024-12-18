package org.skriptlang.skript.lang.comparator;

/**
 * Holds information about a Comparator.
 *
 * @param <T1> The first type for comparison.
 * @param <T2> The second type for comparison.
 */
public final class ComparatorInfo<T1, T2> {

	private final Class<T1> firstType;
	private final Class<T2> secondType;
	private final Comparator<T1, T2> comparator;

	ComparatorInfo(Class<T1> firstType, Class<T2> secondType, Comparator<T1, T2> comparator) {
		this.firstType = firstType;
		this.secondType = secondType;
		this.comparator = comparator;
	}

	/**
	 * @return The first type for comparison for this Comparator.
	 */
	public Class<T1> getFirstType() {
		return firstType;
	}

	/**
	 * @return The second type for comparison for this Comparator.
	 */
	public Class<T2> getSecondType() {
		return secondType;
	}

	/**
	 * @return The Comparator this information is in reference to.
	 */
	public Comparator<T1, T2> getComparator() {
		return comparator;
	}

	@Override
	public String toString() {
		return "ComparatorInfo{first=" + firstType + ",second=" + secondType + ",comparator=" + comparator + "}";
	}

}
