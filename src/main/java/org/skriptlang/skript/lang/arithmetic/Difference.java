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
package org.skriptlang.skript.lang.arithmetic;

@FunctionalInterface
public interface Difference<A, R> {

	R difference(A first, A second);

	final class DifferenceInfo<A, R> {

		private final Class<A> type;
		private final Class<R> relativeType;
		private final Difference<A, R> difference;

		public DifferenceInfo(Class<A> type, Class<R> relativeType, Difference<A, R> difference) {
			this.type = type;
			this.relativeType = relativeType;
			this.difference = difference;
		}

		public Class<A> getType() {
			return type;
		}

		public Class<R> getRelativeType() {
			return relativeType;
		}

		public Difference<A, R> getDifference() {
			return difference;
		}

	}
}
