package org.skriptlang.skript.registration;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.Map.Entry;

final class TypeRegistryImpl implements TypeRegistry {

	private final Map<String, TypeInfo<?>> patternToType = new HashMap<>();
	private final Map<Class<?>, TypeInfo<?>> classToType = new HashMap<>();
	private final Map<Class<?>, TypeInfo<?>> classToSuper = new HashMap<>();

	@Override
	public <I extends TypeInfo<?>> void register(@NotNull I type) {
		for (String pattern : type.patterns()) {
			register(patternToType, pattern, type);
		}
		register(classToType, type.type(), type);
	}

	private <K, V extends TypeInfo<?>> void register(@NotNull Map<K, V> map, @NotNull K key, @NotNull V value) {
		if (map.putIfAbsent(key, value) != null) {
			throw new IllegalArgumentException("Type with key %s already exists".formatted(key));
		}
	}

	@Override
	public void unregister(@NotNull TypeInfo<?> info) {
		for (String pattern : info.patterns()) {
			patternToType.remove(pattern);
		}
		classToType.remove(info.type());
	}

	@Override
	public @Unmodifiable @NotNull Collection<TypeInfo<?>> elements() {
		return Collections.unmodifiableCollection(classToType.values());
	}

	@Override
	public <T> TypeInfo<T> fromPattern(@NotNull String pattern) {
		Preconditions.checkNotNull(pattern, "input cannot be null");

		//noinspection unchecked
		return (TypeInfo<T>) patternToType.get(pattern);
	}

	@Override
	public <T> TypeInfo<T> fromClass(@NotNull Class<T> cls) {
		Preconditions.checkNotNull(cls, "cls cannot be null");

		//noinspection unchecked
		return (TypeInfo<T>) classToType.get(cls);
	}

	@Override
	public @NotNull <T> TypeInfo<? extends T> superTypeFromClass(@NotNull Class<T> cls) {
		Preconditions.checkNotNull(cls, "cls cannot be null");

		TypeInfo<?> info = classToSuper.get(cls);
		if (info != null) {
			//noinspection unchecked
			return (TypeInfo<? extends T>) info;
		}

		for (Entry<Class<?>, TypeInfo<?>> entry : classToType.entrySet()) {
			Class<?> k = entry.getKey();
			TypeInfo<?> v = entry.getValue();
			if (k.isAssignableFrom(cls)) {
				classToSuper.put(k, v);
				//noinspection unchecked
				return (TypeInfo<? extends T>) v;
			}
		}

		//noinspection unchecked
		return (TypeInfo<? extends T>) classToType.values().stream().findAny().orElseThrow();
	}

	record UnmodifiableRegistry(TypeRegistry registry) implements TypeRegistry {

		@Override
		public <I extends TypeInfo<?>> void register(@NotNull I info) {
			throw new UnsupportedOperationException("Cannot register type infos with an unmodifiable type registry.");
		}

		@Override
		public void unregister(@NotNull TypeInfo<?> info) {
			throw new UnsupportedOperationException("Cannot unregister type infos with an unmodifiable type registry.");
		}

		@Override
		public @Unmodifiable @NotNull Collection<TypeInfo<?>> elements() {
			return registry.elements();
		}

		@Override
		public <T> TypeInfo<T> fromPattern(@NotNull String pattern) {
			return registry.fromPattern(pattern);
		}

		@Override
		public <T> TypeInfo<T> fromClass(@NotNull Class<T> cls) {
			return registry.fromClass(cls);
		}

		@Override
		public @NotNull <T> TypeInfo<? extends T> superTypeFromClass(@NotNull Class<T> cls) {
			return registry.superTypeFromClass(cls);
		}

	}

}
