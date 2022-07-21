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
package org.skriptlang.skript.lang.converter;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Used to convert an object to a different type.
 *
 * @param <From> The type to convert from.
 * @param <To> The type to convert to.
 */
@FunctionalInterface
public interface Converter<From, To> {

	/**
	 * Disallow other converters from being chained to this.
	 */
	int NO_LEFT_CHAINING = 1;

	/**
	 * Disallow chaining this with other converters.
	 */
	int NO_RIGHT_CHAINING = 2;

	/**
	 * Disallow all chaining.
	 */
	int NO_CHAINING = NO_LEFT_CHAINING | NO_RIGHT_CHAINING;

	// TODO javadoc
	int NO_COMMAND_ARGUMENTS = 4;

	/**
	 * Converts an object using this Converter.
	 * @param from The object to convert.
	 * @return The converted object.
	 */
	@Nullable To convert(From from);

}
