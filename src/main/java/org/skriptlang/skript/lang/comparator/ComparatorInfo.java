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
 * Holds information about a comparator.
 *
 * @param <Type1> The first type for comparison.
 * @param <Type2> The second type for comparison.
 */
final class ComparatorInfo<Type1, Type2> {

	final Class<Type1> firstType;
	final Class<Type2> secondType;
	final Comparator<Type1, Type2> comparator;

	public ComparatorInfo(Class<Type1> firstType, Class<Type2> secondType, Comparator<Type1, Type2> comparator) {
		this.firstType = firstType;
		this.secondType = secondType;
		this.comparator = comparator;
	}

}
