package org.skriptlang.skript.registration;

import ch.njol.skript.lang.SyntaxElement;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.util.ClassUtils;
import org.skriptlang.skript.util.Priority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

class SyntaxInfoImpl<T extends SyntaxElement> implements SyntaxInfo<T> {

	private final SyntaxOrigin origin;
	private final Class<T> type;
	private final @Nullable Supplier<T> supplier;
	private final Collection<String> patterns;
	private final Priority priority;

	protected SyntaxInfoImpl(
		SyntaxOrigin origin, Class<T> type, @Nullable Supplier<T> supplier,
		Collection<String> patterns, Priority priority
	) {
		Preconditions.checkArgument(supplier != null || ClassUtils.isNormalClass(type),
				"Failed to register a syntax info for '" + type.getName() + "'."
				+ " Element classes must be a normal type unless a supplier is provided.");
		Preconditions.checkArgument(!patterns.isEmpty(),
				"Failed to register a syntax info for '" + type.getName() + "'."
				+ " There must be at least one pattern.");
		this.origin = origin;
		this.type = type;
		this.supplier = supplier;
		this.patterns = ImmutableList.copyOf(patterns);
		this.priority = priority;
	}

	@Override
	public Builder<? extends Builder<?, T>, T> builder() {
		var builder = new BuilderImpl<>(type);
		builder.origin(origin);
		if (supplier != null) {
			builder.supplier(supplier);
		}
		builder.addPatterns(patterns);
		builder.priority(priority);
		return builder;
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
	public @Unmodifiable Collection<String> patterns() {
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
		return other instanceof SyntaxInfo<?> info &&
				Objects.equals(origin(), info.origin()) &&
				Objects.equals(type(), info.type()) &&
				Objects.equals(patterns(), info.patterns()) &&
				Objects.equals(priority(), info.priority());
	}

	@Override
	public int hashCode() {
		return Objects.hash(origin(), type(), patterns(), priority());
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("origin", origin())
				.add("type", type())
				.add("patterns", patterns())
				.add("priority", priority())
				.toString();
	}

	@SuppressWarnings("unchecked")
	static class BuilderImpl<B extends Builder<B, E>, E extends SyntaxElement> implements Builder<B, E> {

		/**
		 * A default origin that describes the class of a syntax.
		 */
		private static final class ClassOrigin implements SyntaxOrigin {

			private final String name;

			ClassOrigin(Class<?> clazz) {
				this.name = clazz.getName();
			}

			@Override
			public String name() {
				return name;
			}

		}

		final Class<E> type;
		SyntaxOrigin origin;
		@Nullable Supplier<E> supplier;
		final List<String> patterns = new ArrayList<>();
		Priority priority = SyntaxInfo.COMBINED;

		BuilderImpl(Class<E> type) {
			this.type = type;
			origin = new ClassOrigin(type);
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

		@Override
		public B clearPatterns() {
			this.patterns.clear();
			return (B) this;
		}

		@Override
		public B priority(Priority priority) {
			this.priority = priority;
			return (B) this;
		}

		public SyntaxInfo<E> build() {
			return new SyntaxInfoImpl<>(origin, type, supplier, patterns, priority);
		}

		@Override
		public void applyTo(Builder<?, ?> builder) {
			builder.origin(origin);
			if (supplier != null) {
				//noinspection rawtypes - Let's hope the user knows what they are doing...
				builder.supplier((Supplier) supplier);
			}
			builder.addPatterns(patterns);
			builder.priority(priority);
		}

	}

}
