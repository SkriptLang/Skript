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

import ch.njol.skript.lang.SyntaxElement;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.lang.Priority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

class SyntaxInfoImpl<T extends SyntaxElement> implements SyntaxInfo<T> {

	private final SyntaxOrigin origin;
	private final Class<T> type;
	@Nullable
	private final Supplier<T> supplier;
	private final Collection<String> patterns;
	private final Priority priority = new Priority();

	protected SyntaxInfoImpl(SyntaxOrigin origin, Class<T> type, @Nullable Supplier<T> supplier, Collection<String> patterns) {
		this.origin = origin;
		this.type = type;
		this.supplier = supplier;
		this.patterns = ImmutableList.copyOf(patterns);
	}

	@Override
	public SyntaxOrigin origin() {
		return origin;
	}

	@Override
	public Class<T> type() {
		return type;
	}

	@Override
	public T instance() {
		try {
			return supplier == null ? type.getDeclaredConstructor().newInstance() : supplier.get();
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	@Unmodifiable
	public Collection<String> patterns() {
		return patterns;
	}

	@Override
	public Priority priority() {
		return priority;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof SyntaxInfo)) {
			return false;
		}
		SyntaxInfo<?> info = (SyntaxInfo<?>) other;
		return Objects.equals(origin(), info.origin()) &&
				Objects.equals(type(), info.type()) &&
				Objects.equals(patterns(), info.patterns());
	}

	@Override
	public int hashCode() {
		return Objects.hash(origin(), type(), patterns());
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("origin", origin())
				.add("type", type())
				.add("patterns", patterns())
				.toString();
	}

	@SuppressWarnings("unchecked")
	static class BuilderImpl<B extends Builder<B, E>, E extends SyntaxElement> implements Builder<B, E> {

		final Class<E> type;
		final List<String> patterns = new ArrayList<>();
		@Nullable
		Supplier<E> supplier;
		SyntaxOrigin origin = SyntaxOrigin.UNKNOWN;

		BuilderImpl(Class<E> type) {
			this.type = type;
		}

		public B origin(SyntaxOrigin origin) {
			this.origin = origin;
			return (B) this;
		}

		public B supplier(Supplier<E> supplier) {
			this.supplier = supplier;
			return (B) this;
		}

		public B addPattern(String pattern) {
			this.patterns.add(pattern);
			return (B) this;
		}

		public B addPatterns(String... patterns) {
			Collections.addAll(this.patterns, patterns);
			return (B) this;
		}

		public B addPatterns(Collection<String> patterns) {
			this.patterns.addAll(patterns);
			return (B) this;
		}

		public SyntaxInfo<E> build() {
			return new SyntaxInfoImpl<>(origin, type, supplier, patterns);
		}

	}

}
