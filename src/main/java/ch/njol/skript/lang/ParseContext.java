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
