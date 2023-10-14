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
package ch.njol.skript.lang;

import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.expressions.base.PropertyExpression;

/**
 * Used to define in which order to parse expressions.
 */
public enum ExpressionType {

	/**
	 * Expressions that only match simple text, e.g. "[the] player"
	 */
	SIMPLE,

	/**
	 * Expressions that are related to the Event that are typically simple.
	 * 
	 * @see EventValueExpression
	 */
	EVENT,

	/**
	 * Expressions that contain other expressions, e.g. "[the] distance between %location% and %location%"
	 * 
	 * @see #PROPERTY
	 */
	COMBINED,

	/**
	 * Property expressions, e.g. "[the] data value[s] of %items%"/"%items%'[s] data value[s]"
	 * 
	 * @see PropertyExpression
	 */
	PROPERTY,

	/**
	 * Expressions whose pattern matches (almost) everything. Typically when using regex. Example: "[the] [loop-]<.+>"
	 */
	PATTERN_MATCHES_EVERYTHING;

}
