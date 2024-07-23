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
package ch.njol.skript.classes;

/**
 * @author Peter Güttinger
 * @deprecated This class is no longer exposed in newer versions. It should not be used or referenced.
 */
@Deprecated
public class InverseComparator<T1, T2> implements Comparator<T1, T2> {

	private final Comparator<? super T2, ? super T1> comp;

	public InverseComparator(final Comparator<? super T2, ? super T1> c) {
		comp = c;
	}

	@Override
	public Relation compare(final T1 o1, final T2 o2) {
		return comp.compare(o2, o1).getSwitched();
	}

	@Override
	public boolean supportsOrdering() {
		return comp.supportsOrdering();
	}

	@Override
	public String toString() {
		return "InverseComparator(" + comp + ")";
	}

}
