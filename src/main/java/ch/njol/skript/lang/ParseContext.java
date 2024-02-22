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

/**
 * Used to provide context as to where an element is being parsed from.
 */
public enum ParseContext {

	/**
	 * Default parse mode
	 */
	DEFAULT,

	/**
	 * Used for parsing events of triggers.
	 * <p>
	 * TODO? replace with {@link #DUMMY} + {@link SkriptParser#PARSE_LITERALS}
	 */
	EVENT,

	/**
	 * Only used for parsing arguments of commands
	 */
	COMMAND,

	/**
	 * Used for parsing text in {@link ch.njol.skript.expressions.ExprParse}
	 */
	PARSE,
	/**
	 * Used for parsing values from a config
	 */
	CONFIG,

	/**
	 * Used for parsing variables in a script's variables section
	 */
	SCRIPT

}
