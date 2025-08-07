package org.skriptlang.skript.common.function;

import ch.njol.skript.lang.Expression;

import java.util.Set;

/**
 * A parameter for a {@link DefaultFunction}.
 *
 * @param name The name.
 * @param type The type's class.
 * @param modifiers The modifiers.
 * @param defaultValue The default value, or null if there is no default value.
 * @param <T> The type.
 */
public record ScriptParameter<T>(String name, Class<T> type, Set<Modifier> modifiers, Expression<?> defaultValue)
	implements Parameter<T> {

	public ScriptParameter(String name, Class<T> type, Modifier... modifiers) {
		this(name, type, Set.of(modifiers), null);
	}

	public ScriptParameter(String name, Class<T> type, Expression<?> defaultValue, Modifier... modifiers) {
		this(name, type, Set.of(modifiers), defaultValue);
	}

}

