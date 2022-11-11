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

/**
 * Used to get the difference between two objects of the same type.
 * @param <A> The absolute type
 * @param <R> The relative type
 * @see Arithmetic
 * @see Arithmetics#registerDifference(Class, Class, Difference)
 * @see ch.njol.skript.classes.data.DefaultArithmetics
 */
@FunctionalInterface
public interface Difference<A, R> {

	R difference(A first, A second);

}
