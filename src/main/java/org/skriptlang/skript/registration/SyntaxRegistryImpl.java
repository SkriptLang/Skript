package org.skriptlang.skript.registration;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.docs.DocumentationAdapter;
import org.skriptlang.skript.docs.Origin;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

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
	@SuppressWarnings({"unchecked", "rawtypes"})
	public void unregister(SyntaxInfo info) {
		for (Key key : registers.keySet()) {
			unregister(key, info);
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
		ImmutableSet.Builder<@NotNull SyntaxInfo<?>> builder = ImmutableSet.builder();
		registers.values().forEach(register -> {
			synchronized (register.syntaxes) {
				builder.addAll(register.syntaxes);
			}
		});
		return builder.build();
	}

	@Override
	public void write(DocumentationAdapter adapter) {
		write(adapter, registers.keySet().stream().filter(key -> key != SyntaxRegistry.STATEMENT).toList());
	}

	static final class OriginApplyingRegistry implements SyntaxRegistry {

		private final SyntaxRegistry syntaxRegistry;
		private final @Nullable Origin origin;
		private final @Nullable Function<SyntaxInfo<?>, Origin> originFactory;

		OriginApplyingRegistry(SyntaxRegistry syntaxRegistry, @NotNull Origin origin) {
			this.syntaxRegistry = syntaxRegistry;
			this.origin = origin;
			this.originFactory = null;
		}

		OriginApplyingRegistry(SyntaxRegistry syntaxRegistry, @NotNull Function<SyntaxInfo<?>, Origin> originFactory) {
			this.syntaxRegistry = syntaxRegistry;
			this.origin = null;
			this.originFactory = originFactory;
		}

		@Override
		public @Unmodifiable <I extends SyntaxInfo<?>> Collection<I> syntaxes(Key<I> key) {
			return syntaxRegistry.syntaxes(key);
		}

		@Override
		public <I extends SyntaxInfo<?>> void register(Key<I> key, I info) {
			if (info.documentation().origin() == Origin.UNKNOWN) { // when origin is unspecified, add one
				Origin origin = this.origin;
				if (origin == null) {
					assert originFactory != null;
					origin = originFactory.apply(info);
				}
				//noinspection unchecked
				info = (I) info.toBuilder()
					.documentation(info.documentation().toBuilder()
						.origin(origin)
						.build())
					.build();
			}
			syntaxRegistry.register(key, info);
		}

		@Override
		public void unregister(SyntaxInfo<?> info) {
			syntaxRegistry.unregister(info);
		}

		@Override
		public <I extends SyntaxInfo<?>> void unregister(Key<I> key, I info) {
			syntaxRegistry.unregister(key, info);
		}

		@Override
		public @Unmodifiable Collection<SyntaxInfo<?>> elements() {
			return syntaxRegistry.elements();
		}

		@Override
		public void write(DocumentationAdapter adapter) {
			syntaxRegistry.write(adapter);
		}

		@Override
		public void write(DocumentationAdapter adapter, Iterable<Key<?>> keys) {
			syntaxRegistry.write(adapter, keys);
		}

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
		public void unregister(SyntaxInfo<?> info) {
			throw new UnsupportedOperationException("Cannot unregister syntax infos from an unmodifiable syntax registry.");
		}

		@Override
		public <I extends SyntaxInfo<?>> void unregister(Key<I> key, I info) {
			throw new UnsupportedOperationException("Cannot unregister syntax infos from an unmodifiable syntax registry.");
		}

		@Override
		public SyntaxRegistry unmodifiableView() {
			return this;
		}

		@Override
		public void write(DocumentationAdapter adapter) {
			registry.write(adapter);
		}

		@Override
		public void write(DocumentationAdapter adapter, Iterable<Key<?>> keys) {
			registry.write(adapter, keys);
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
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			return other instanceof Key<?> key &&
					name().equals(key.name());
		}

		@Override
		public int hashCode() {
			return name().hashCode();
		}

		@Override
		public String toString() {
			return MoreObjects.toStringHelper(this)
					.add("name", name())
					.toString();
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

		@Override
		public boolean equals(Object other) {
			return other instanceof ChildKey<?, ?> key &&
					super.equals(other) &&
					parent().equals(key.parent());
		}

		@Override
		public int hashCode() {
			return Objects.hash(super.hashCode(), parent());
		}

		@Override
		public String toString() {
			return MoreObjects.toStringHelper(this)
					.add("name", name())
					.add("parent", parent())
					.toString();
		}

	}

}
