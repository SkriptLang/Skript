package org.skriptlang.skript.lang.function;

import java.util.Set;

/**
 * A parameter for a {@link DefaultFunction}.
 *
 * @param name The name.
 * @param type The type's class.
 * @param modifiers The modifiers.
 * @param <T> The type.
 */
public record DefaultParameter<T>(String name, Class<T> type, Set<Modifier> modifiers)
	implements Parameter<T> {

	public DefaultParameter(String name, Class<T> type, Modifier... modifiers) {
		this(name, type, Set.of(modifiers));
	}

}