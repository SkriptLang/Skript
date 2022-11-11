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
import org.jetbrains.annotations.Nullable;

/**
 * Represents arithmetic for certain two types. Multiplication, division and
 * power of methods are optional and may throw UnsupportedOperationExceptions.
 * @param <A> the type of the absolute value
 * @param <R> the type of the relative value
 */
public interface Arithmetic<A> {

	Class<?> @Nullable [] acceptOperator(Operator operator);

	A calculate(A first, Operator operator, Object second);

	final class ArithmeticInfo<T> {

		private final Class<T> type;
		private final Arithmetic<T> arithmetic;

		public ArithmeticInfo(Class<T> type, Arithmetic<T> arithmetic) {
			this.type = type;
			this.arithmetic = arithmetic;
		}

		public Class<T> getType() {
			return type;
		}

		public Arithmetic<T> getArithmetic() {
			return arithmetic;
		}

	}
}
