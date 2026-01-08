package org.skriptlang.skript.lang.comparator;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

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

	ComparatorInfo(
		@NotNull Class<T1> firstType,
		@NotNull Class<T2> secondType,
		@NotNull Comparator<T1, T2> comparator
	) {
		Preconditions.checkNotNull(firstType, "Cannot create a comparison between nothing and something! (firstType is null)");
		Preconditions.checkNotNull(secondType, "Cannot create a comparison between something and nothing! (secondType is null)");
		Preconditions.checkNotNull(comparator, "Cannot create a comparison with a null comparator!");
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
