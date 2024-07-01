/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
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
