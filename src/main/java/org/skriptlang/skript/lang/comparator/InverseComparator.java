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
 * Similar to {@link Comparator}, but {@link Comparator#compare(Object, Object)} arguments are switched.
 * If necessary, the resulting {@link Relation} is switched
 */
final class InverseComparator<Type1, Type2> implements Comparator<Type1, Type2> {

	private final Comparator<Type2, Type1> comparator;

	InverseComparator(Comparator<Type2, Type1> comparator) {
		this.comparator = comparator;
	}

	@Override
	public Relation compare(Type1 o1, Type2 o2) {
		return comparator.compare(o2, o1).getSwitched();
	}

	@Override
	public boolean supportsOrdering() {
		return comparator.supportsOrdering();
	}

	@Override
	public boolean supportsInversion() {
		return false;
	}

	@Override
	public String toString() {
		return "InverseComparator{" + comparator + "}";
	}

}
