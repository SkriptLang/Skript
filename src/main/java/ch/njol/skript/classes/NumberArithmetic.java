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
 */
@Deprecated
public class NumberArithmetic implements Arithmetic<Number, Number> {
	
	@Override
	public Number difference(final Number first, final Number second) {
		double result = Math.abs(first.doubleValue() - second.doubleValue());
		if (result == (long) result)
			return (long) result;
		return result;
	}
	
	@Override
	public Number add(final Number value, final Number difference) {
		double result = value.doubleValue() + difference.doubleValue();
		if (result == (long) result)
			return (long) result;
		return result;
	}
	
	@Override
	public Number subtract(final Number value, final Number difference) {
		double result = value.doubleValue() - difference.doubleValue();
		if (result == (long) result)
			return (long) result;
		return result;
	}

	@Override
	public Number multiply(Number value, Number multiplier) {
		double result = value.doubleValue() * multiplier.doubleValue();
		if (result == (long) result)
			return (long) result;
		return result;
	}

	@Override
	public Number divide(Number value, Number divider) {
		double result = value.doubleValue() / divider.doubleValue();
		if (result == (long) result)
			return (long) result;
		return result;
	}

	@Override
	public Number power(Number value, Number exponent) {
		double result = Math.pow(value.doubleValue(), exponent.doubleValue());
		if (result == (long) result)
			return (long) result;
		return result;
	}
	
}
