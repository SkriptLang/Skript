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
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

/**
 * An enum containing {@link Script} warnings that can be suppressed.
 */
public enum ScriptWarning {

	/**
	 * Possible variable conflict (Deprecated)
	 */
	@Deprecated
	VARIABLE_CONFLICT("conflict", "conflict", "Variable conflict warnings no longer need suppression, as they have been removed altogether"),

	/**
	 * Variable cannot be saved (the ClassInfo is not serializable)
	 */
	VARIABLE_SAVE("variable save"),

	/**
	 * Missing "and" or "or"
	 */
	MISSING_CONJUNCTION("[missing] conjunction", "missing conjunction"),

	/**
	 * Variable starts with an Expression
	 */
	VARIABLE_STARTS_WITH_EXPRESSION("starting [with] expression[s]", "starting expression"),

	/**
	 * This syntax is deprecated and scheduled for future removal
	 */
	DEPRECATED_SYNTAX("deprecated syntax"),

	/**
	 * The code cannot be reached due to a previous statement stopping further execution
	 */
	UNREACHABLE_CODE("unreachable code");

	ScriptWarning(String pattern) {
		this(pattern, pattern);
	}

	ScriptWarning(String pattern, String warningName) {
		this(pattern, warningName, null);
	}

	ScriptWarning(String pattern, String warningName, @Nullable String deprecationMessage) {
		this.pattern = pattern;
		this.warningName = warningName;
		this.deprecationMessage = deprecationMessage;
	}

	private final String pattern;
	private final String warningName;
	private final @UnknownNullability String deprecationMessage;

	public String getPattern() {
		return pattern;
	}

	public String getWarningName() {
		return warningName;
	}

	public boolean isDeprecated() {
		return deprecationMessage != null;
	}

	public @UnknownNullability String getDeprecationMessage() {
		return deprecationMessage;
	}

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
