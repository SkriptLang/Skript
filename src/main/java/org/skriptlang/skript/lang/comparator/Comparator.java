package org.skriptlang.skript.lang.comparator;

/**
 * Used to compare two objects of a different or the same type.
 *
 * @param <T1> The first type for comparison.
 * @param <T2> The second type for comparison.
 * @see Comparators#registerComparator(Class, Class, Comparator)
 */
@FunctionalInterface
public interface Comparator<T1, T2> {

	/**
	 * The main method for this Comparator to determine the Relation between two objects.
	 * @param o1 The first object for comparison.
	 * @param o2 The second object for comparison.
	 * @return The Relation between the two provided objects.
	 */
	Relation compare(T1 o1, T2 o2);
	
	/**
	 * @return Whether this comparator supports ordering of elements or not.
	 */
	default boolean supportsOrdering() {
		return false;
	}

	/**
	 * @return Whether this comparator supports argument inversion through {@link InverseComparator}.
	 */
	default boolean supportsInversion() {
		return true;
	}
	
}
