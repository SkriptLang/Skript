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
package ch.njol.skript.expressions.arithmetic;

@SuppressWarnings("UnnecessaryBoxing")
@Deprecated
public enum Operator {
	
	PLUS('+') {
		@SuppressWarnings("null")
		@Override
		public Number calculate(final Number n1, final Number n2, final boolean integer) {
			if (integer)
				return Long.valueOf(n1.longValue() + n2.longValue());
			return Double.valueOf(n1.doubleValue() + n2.doubleValue());
		}
	},
	MINUS('-') {
		@SuppressWarnings("null")
		@Override
		public Number calculate(final Number n1, final Number n2, final boolean integer) {
			if (integer)
				return Long.valueOf(n1.longValue() - n2.longValue());
			return Double.valueOf(n1.doubleValue() - n2.doubleValue());
		}
	},
	MULT('*') {
		@SuppressWarnings("null")
		@Override
		public Number calculate(final Number n1, final Number n2, final boolean integer) {
			if (integer)
				return Long.valueOf(n1.longValue() * n2.longValue());
			return Double.valueOf(n1.doubleValue() * n2.doubleValue());
		}
	},
	DIV('/') {
		@SuppressWarnings("null")
		@Override
		public Number calculate(final Number n1, final Number n2, final boolean integer) {
			if (integer) {
				final long div = n2.longValue();
				if (div == 0)
					return Long.MAX_VALUE;
				return Long.valueOf(n1.longValue() / div);
			}
			return Double.valueOf(n1.doubleValue() / n2.doubleValue());
		}
	},
	EXP('^') {
		@SuppressWarnings("null")
		@Override
		public Number calculate(final Number n1, final Number n2, final boolean integer) {
			if (integer)
				return Long.valueOf((long) Math.pow(n1.longValue(), n2.longValue()));
			return Double.valueOf(Math.pow(n1.doubleValue(), n2.doubleValue()));
		}
	};
	
	private final char sign;
	
	Operator(final char sign) {
		this.sign = sign;
	}
	
	public abstract Number calculate(Number n1, Number n2, boolean integer);
	
	@Override
	public String toString() {
		return "" + sign;
	}
	
}
