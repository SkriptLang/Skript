/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package org.skriptlang.skript.registration;

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
		return (SyntaxRegister<I>) registers.computeIfAbsent(key, k -> new SyntaxRegisterImpl<>());
	}

	static final class ChildSyntaxRegistryImpl implements ChildSyntaxRegistry {

		private final SyntaxRegistry parent;
		private final SyntaxRegistry child;

		ChildSyntaxRegistryImpl(SyntaxRegistry parent, SyntaxRegistry child) {
			this.parent = parent;
			this.child = child;
		}

		@Override
		@Unmodifiable
		public <I extends SyntaxInfo<?>> Collection<I> syntaxes(Key<I> key) {
			return child.syntaxes(key);
		}

		@Override
		public <I extends SyntaxInfo<?>> void register(Key<I> key, I info) {
			parent.register(key, info);
			child.register(key, info);
		}

		@Override
		public <I extends SyntaxInfo<?>> void unregister(Key<I> key, I info) {
			parent.unregister(key, info);
			child.unregister(key, info);
		}

		@Override
		public SyntaxRegistry parent() {
			return parent;
		}

	}

	static final class UnmodifiableRegistry implements SyntaxRegistry {

		private final SyntaxRegistry registry;

		UnmodifiableRegistry(SyntaxRegistry registry) {
			this.registry = registry;
		}

		@Override
		@Unmodifiable
		public <I extends SyntaxInfo<?>> Collection<I> syntaxes(Key<I> key) {
			return registry.syntaxes(key);
		}

		@Override
		public <I extends SyntaxInfo<?>> void register(Key<I> key, I info) {
			throw new UnsupportedOperationException("An unmodifiable registry cannot have syntax infos added.");
		}

		@Override
		public <I extends SyntaxInfo<?>> void unregister(Key<I> key, I info) {
			throw new UnsupportedOperationException("An unmodifiable registry cannot have syntax infos removed.");
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
