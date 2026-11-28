package ch.njol.skript.variables;

import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.variables.VariableMap;

/**
 * @deprecated Replaced by {@link org.skriptlang.skript.variables.VariableMap}
 */
@Deprecated(since = "INSERT VERSION", forRemoval = true)
final class VariablesMap {
	final VariableMap newMap;

	VariablesMap(VariableMap map) {
		this.newMap = map;
	}

	static @Nullable VariablesMap from(@Nullable VariableMap newMap) {
		if (newMap == null)
			return null;
		return new VariablesMap(newMap);
	}
}
