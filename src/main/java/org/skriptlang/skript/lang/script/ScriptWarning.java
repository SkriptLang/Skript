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
package org.skriptlang.skript.lang.script;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.util.Kleenean;

/**
 * An enum containing {@link Script} warnings that can be suppressed.
 */
public enum ScriptWarning {

	/**
	 * Variable cannot be saved (the ClassInfo is not serializable)
	 */
	VARIABLE_SAVE,

	/**
	 * Missing "and" or "or"
	 */
	MISSING_CONJUNCTION,

	/**
	 * Variable starts with an Expression
	 */
	VARIABLE_STARTS_WITH_EXPRESSION,

	/**
	 * This syntax is deprecated and scheduled for future removal
	 */
	DEPRECATED_SYNTAX;

	/**
	 * Prints the given message using {@link Skript#warning(String)} iff the current script does not suppress deprecation warnings.
	 * Intended for use in {@link ch.njol.skript.lang.SyntaxElement#init(Expression[], int, Kleenean, SkriptParser.ParseResult)}.
	 * The given message is prefixed with {@code "[Deprecated] "} to provide a common link between deprecation warnings.
	 *
	 * @param message the warning message to print.
	 */
	public static void printDeprecationWarning(String message) {
		ParserInstance parser = ParserInstance.get();
		Script currentScript = parser.isActive() ? parser.getCurrentScript() : null;
		if (currentScript != null && currentScript.suppressesWarning(ScriptWarning.DEPRECATED_SYNTAX))
			return;
		Skript.warning("[Deprecated] " + message);
	}

}
