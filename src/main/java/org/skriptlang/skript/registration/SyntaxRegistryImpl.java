package org.skriptlang.skript.registration;

import com.google.common.collect.ImmutableSet;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

final class SyntaxRegistryImpl implements SyntaxRegistry {

	private final Map<Key<?>, SyntaxRegister<?>> registers = new ConcurrentHashMap<>();

	@Override
	@Unmodifiable
	public <I extends SyntaxInfo<?>> Collection<I> syntaxes(Key<I> key) {
		return register(key).syntaxes();
	}

	@Override
	public <I extends SyntaxInfo<?>> void register(Key<I> key, I info) {
		register(key).add(info);
		if (key instanceof ChildKey) {
			register(((ChildKey<? extends I, I>) key).parent(), info);
		}
	}

	@Override
	public <I extends SyntaxInfo<?>> void unregister(Key<I> key, I info) {
		register(key).remove(info);
		if (key instanceof ChildKey) {
			unregister(((ChildKey<? extends I, I>) key).parent(), info);
		}
	}

	@SuppressWarnings("unchecked")
	private <I extends SyntaxInfo<?>> SyntaxRegister<I> register(Key<I> key) {
		return (SyntaxRegister<I>) registers.computeIfAbsent(key, k -> new SyntaxRegister<>());
	}

	@Override
	public Collection<SyntaxInfo<?>> elements() {
		ImmutableSet.Builder<SyntaxInfo<?>> builder = ImmutableSet.builder();
		registers.values().forEach(register -> {
			synchronized (register.syntaxes) {
				builder.addAll(register.syntaxes);
			}
		});
		return builder.build();
	}

	static final class UnmodifiableRegistry implements SyntaxRegistry {

		private final SyntaxRegistry registry;

		UnmodifiableRegistry(SyntaxRegistry registry) {
			this.registry = registry;
		}

		@Override
		public @Unmodifiable Collection<SyntaxInfo<?>> elements() {
			return registry.elements();
		}

		@Override
		public @Unmodifiable <I extends SyntaxInfo<?>> Collection<I> syntaxes(Key<I> key) {
			return registry.syntaxes(key);
		}

		@Override
		public <I extends SyntaxInfo<?>> void register(Key<I> key, I info) {
			throw new UnsupportedOperationException("Cannot register syntax infos with an unmodifiable syntax registry.");
		}

		@Override
		public <I extends SyntaxInfo<?>> void unregister(Key<I> key, I info) {
			throw new UnsupportedOperationException("Cannot unregister syntax infos from an unmodifiable syntax registry.");
		}

	}

	static class KeyImpl<T extends SyntaxInfo<?>> implements Key<T> {

		protected final String name;

		KeyImpl(String name) {
			this.name = name;
		}

		@Override
		public String name() {
			return name;
		}

		@Override
		public int hashCode() {
			return name.hashCode();
		}

		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (!(other instanceof Key<?>)) {
				return false;
			}
			Key<?> key = (Key<?>) other;
			return name().equals(key.name());
		}

		@Override
		public String toString() {
			return name;
		}

	}

	static final class ChildKeyImpl<T extends P, P extends SyntaxInfo<?>> extends KeyImpl<T> implements ChildKey<T, P> {

		private final Key<P> parent;

		ChildKeyImpl(Key<P> parent, String name) {
			super(name);
			this.parent = parent;
		}

		@Override
		public Key<P> parent() {
			return parent;
		}

	}

}
