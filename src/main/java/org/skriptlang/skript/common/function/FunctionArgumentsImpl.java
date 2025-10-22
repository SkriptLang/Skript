package org.skriptlang.skript.common.function;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

record FunctionArgumentsImpl(@Unmodifiable @NotNull Map<String, Object> arguments) implements FunctionArguments {

	FunctionArgumentsImpl {
		Preconditions.checkNotNull(arguments, "arguments cannot be null");
	}

	@Override
	public <T> T get(@NotNull String name) {
		Preconditions.checkNotNull(name, "name cannot be null");

		//noinspection unchecked
		return (T) arguments.get(name);
	}

	@Override
	public <T> T getOrDefault(@NotNull String name, T defaultValue) {
		Preconditions.checkNotNull(name, "name cannot be null");

		//noinspection unchecked
		return (T) arguments.getOrDefault(name, defaultValue);
	}

	@Override
	public <T> T getOrDefault(@NotNull String name, Supplier<T> defaultValue) {
		Preconditions.checkNotNull(name, "name cannot be null");

		Object existing = arguments.get(name);
		if (existing == null) {
			return defaultValue.get();
		} else {
			//noinspection unchecked
			return (T) existing;
		}
	}

	@Override
	public @Unmodifiable @NotNull Set<String> names() {
		return Collections.unmodifiableSet(arguments.keySet());
	}

}
