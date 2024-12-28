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
	MISSING_CONJUNCTION("missing conjunction", "[missing] conjunction"),

	/**
	 * Variable starts with an Expression
	 */
	VARIABLE_STARTS_WITH_EXPRESSION("starting expression", "starting [with] expression[s]"),

	/**
	 * This syntax is deprecated and scheduled for future removal
	 */
	DEPRECATED_SYNTAX("deprecated syntax"),

	/**
	 * The code cannot be reached due to a previous statement stopping further execution
	 */
	UNREACHABLE_CODE("unreachable code");

	private final String warningName;
	private final String pattern;
	private final @UnknownNullability String deprecationMessage;

	ScriptWarning(String warningName) {
		this(warningName, warningName);
	}

	ScriptWarning(String warningName, String pattern) {
		this(warningName, pattern, null);
	}

	ScriptWarning(String warningName, String pattern, @Nullable String deprecationMessage) {
		this.warningName = warningName;
		this.pattern = pattern;
		this.deprecationMessage = deprecationMessage;
	}

	public String getWarningName() {
		return warningName;
	}

	public String getPattern() {
		return pattern;
	}

	public boolean isDeprecated() {
		return deprecationMessage != null;
	}

	/**
	 * Returns the deprecation message of this warning, or null if the warning isn't deprecated. 
	 * @return The deprecation message.
	 * @see #isDeprecated()
	 */
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
