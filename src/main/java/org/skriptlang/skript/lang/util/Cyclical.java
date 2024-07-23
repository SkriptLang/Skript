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
package org.skriptlang.skript.lang.util;

/**
 * This is for a special type of numerical value that is compared in a cyclical (rather than a linear) way.
 * <p>
 * The current example of this in Skript is Time,
 * since 23:59 can be both before XOR after 00:01 depending on the context.
 * <p>
 * In practice, cyclical types have to be compared in a special way (particularly for "is between")
 * when we can use the order of operations to determine the context.
 * <p>
 * The minimum/maximum values are intended to help with unusual equality checks, (e.g. 11pm = 1am - 2h).
 *
 * @param <Value> the type of number this uses, to help with type coercion
 */
public interface Cyclical<Value extends Number> {
	
	/**
	 * The potential 'top' of the cycle, e.g. the highest value after which this should restart.
	 * In practice, nothing forces this, so you can write 24:00 or 361° instead of 00:00 and 1° respectively.
	 *
	 * @return the highest legal value
	 */
	Value getMaximum();
	
	/**
	 * The potential 'bottom' of the cycle, e.g. the lowest value.
	 * In practice, nothing forces this, so 24:00 is synonymous with 00:00.
	 *
	 * @return the lowest legal value
	 */
	Value getMinimum();
	
}
