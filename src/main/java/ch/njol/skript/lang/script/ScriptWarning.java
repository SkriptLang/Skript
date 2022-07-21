package ch.njol.skript.lang.script;

/**
 * An enum containing {@link Script} warnings that can be suppressed.
 */
public enum ScriptWarning {

	VARIABLE_SAVE, // Variable cannot be saved (the ClassInfo is not serializable)

	MISSING_CONJUNCTION, // Missing "and" or "or"

	VARIABLE_STARTS_WITH_EXPRESSION // Variable starts with an Expression

}
