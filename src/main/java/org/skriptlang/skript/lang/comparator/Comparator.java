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
