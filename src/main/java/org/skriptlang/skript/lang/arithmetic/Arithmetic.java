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

import ch.njol.skript.expressions.arithmetic.Operator;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Represents arithmetic for certain a certain type.
 * @param <T> the type of arithmetic
 * @see Difference
 * @see Arithmetics#registerArithmetic(Class, Arithmetic)
 * @see ch.njol.skript.classes.data.DefaultArithmetics
 */
public interface Arithmetic<T> {

	/**
	 * Tests whether this arithmetic supports the given operator, and if yes what type(s) it expects the second value to be.
	 * @param operator The operator to test for
	 * @return An array of types that can be used in {@link #calculate(Object, Operator, Object)}.
	 */
	Class<?> @Nullable [] acceptOperator(Operator operator);


	/**
	 * Tests whether this arithmetic supports the given operator and types.
	 * @param operator The operator to test for
	 * @param types The types to test for
	 * @return Whether {@link #calculate(Object, Operator, Object)} can be used or not.
	 */
	default boolean acceptsOperator(Operator operator, Class<?>... types) {
		Class<?>[] classes = acceptOperator(operator);
		if (classes == null)
			return false;
		for (Class<?> type : types) {
			for (Class<?> aClass : classes) {
				if (aClass.isAssignableFrom(type))
					return true;
			}
		}
		return false;
	}

	T calculate(T first, Operator operator, Object second);

}
