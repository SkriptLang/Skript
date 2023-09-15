/**
 * This file is part of Skript.
 * <p>
 * Skript is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Skript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.lang;

/**
 * Used to define in which order to parse expressions.
 *
 * @author Peter Güttinger
 */
public enum ExpressionType {
	/**
	 * Expressions that only match simple text, e.g. "[the] player"
	 */
	SIMPLE,

	/**
	 * I don't know what this was used for. It will be removed or renamed in the future.
	 */
	@Deprecated
	NORMAL,

	/**
	 * Expressions that contain other expressions, e.g. "[the] distance between %location% and %location%"
	 *
	 * @see #PROPERTY
	 */
	COMBINED,

	/**
	 * Property expressions, e.g. "[the] data value[s] of %items%"/"%items%'[s] data value[s]"
	 */
	PROPERTY,

	/**
	 * Expressions whose pattern matches (almost) everything, e.g. "[the] [event-]<.+>"
	 */
	PATTERN_MATCHES_EVERYTHING;
}
