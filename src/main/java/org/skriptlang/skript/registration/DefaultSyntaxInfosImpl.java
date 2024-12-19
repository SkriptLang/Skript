package org.skriptlang.skript.registration;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.entry.EntryValidator;
import org.skriptlang.skript.util.Priority;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Supplier;

final class DefaultSyntaxInfosImpl {

	/**
	 * {@inheritDoc}
	 */
	static class ExpressionImpl<E extends ch.njol.skript.lang.Expression<R>, R>
		extends SyntaxInfoImpl<E> implements DefaultSyntaxInfos.Expression<E, R> {

		private final Class<R> returnType;

		ExpressionImpl(
			SyntaxOrigin origin, Class<E> type, @Nullable Supplier<E> supplier,
			Collection<String> patterns, Priority priority, @Nullable Class<R> returnType
		) {
			super(origin, type, supplier, patterns, priority);
			Preconditions.checkNotNull(returnType, "An expression syntax info must have a return type.");
			this.returnType = returnType;
		}

		@Override
		public Expression.Builder<? extends Expression.Builder<?, E, R>, E, R> builder() {
			var builder = new BuilderImpl<>(type(), returnType);
			super.builder().applyTo(builder);
			return builder;
		}

		@Override
		public Class<R> returnType() {
			return returnType;
		}

		@Override
		public boolean equals(Object other) {
			return other instanceof Expression<?, ?> expression &&
					super.equals(other) &&
					returnType() == expression.returnType();
		}

		@Override
		public int hashCode() {
			return Objects.hash(super.hashCode(), returnType());
		}

		@Override
		public String toString() {
			return MoreObjects.toStringHelper(this)
					.add("origin", origin())
					.add("type", type())
					.add("patterns", patterns())
					.add("priority", priority())
					.add("returnType", returnType())
					.toString();
		}

		/**
		 * {@inheritDoc}
		 */
		static final class BuilderImpl<B extends Expression.Builder<B, E, R>, E extends ch.njol.skript.lang.Expression<R>, R>
			extends SyntaxInfoImpl.BuilderImpl<B, E>
			implements Expression.Builder<B, E, R> {

			private final Class<R> returnType;

			BuilderImpl(Class<E> expressionClass, Class<R> returnType) {
				super(expressionClass);
				this.returnType = returnType;
			}

			public Expression<E, R> build() {
				return new ExpressionImpl<>(origin, type, supplier, patterns, priority, returnType);
			}
		}

	}

	/**
	 * {@inheritDoc}
	 */
	static class StructureImpl<E extends org.skriptlang.skript.lang.structure.Structure>
		extends SyntaxInfoImpl<E> implements DefaultSyntaxInfos.Structure<E> {

		private final @Nullable EntryValidator entryValidator;
		private final NodeType nodeType;

		StructureImpl(
			SyntaxOrigin origin, Class<E> type, @Nullable Supplier<E> supplier,
			Collection<String> patterns, Priority priority,
			@Nullable EntryValidator entryValidator, NodeType nodeType
		) {
			super(origin, type, supplier, patterns, priority);
			if (!nodeType.canBeSection() && entryValidator != null)
				throw new IllegalArgumentException("Simple Structures cannot have an EntryValidator");
			this.entryValidator = entryValidator;
			this.nodeType = nodeType;
		}

		@Override
		public Structure.Builder<? extends Structure.Builder<?, E>, E> builder() {
			var builder = new BuilderImpl<>(type());
			super.builder().applyTo(builder);
			if (entryValidator != null) {
				builder.entryValidator(entryValidator);
			}
			builder.nodeType(nodeType);
			return builder;
		}

		@Override
		public @Nullable EntryValidator entryValidator() {
			return entryValidator;
		}

		@Override
		public NodeType nodeType() {
			return nodeType;
		}

		@Override
		public boolean equals(Object other) {
			return other instanceof Structure<?> structure &&
					super.equals(other) &&
					Objects.equals(entryValidator(), structure.entryValidator()) &&
					Objects.equals(nodeType(), structure.nodeType());
		}

		@Override
		public int hashCode() {
			return Objects.hash(super.hashCode(), entryValidator(), nodeType());
		}

		@Override
		public String toString() {
			return MoreObjects.toStringHelper(this)
					.add("origin", origin())
					.add("type", type())
					.add("patterns", patterns())
					.add("priority", priority())
					.add("entryValidator", entryValidator())
					.toString();
		}

		/**
		 * {@inheritDoc}
		 */
		@SuppressWarnings("unchecked")
		static final class BuilderImpl<B extends Structure.Builder<B, E>, E extends org.skriptlang.skript.lang.structure.Structure>
			extends SyntaxInfoImpl.BuilderImpl<B, E>
			implements Structure.Builder<B, E> {

			private @Nullable EntryValidator entryValidator;
			private NodeType nodeType = NodeType.SECTION;

			BuilderImpl(Class<E> structureClass) {
				super(structureClass);
			}

			@Override
			public B entryValidator(EntryValidator entryValidator) {
				this.entryValidator = entryValidator;
				return (B) this;
			}

			@Override
			public B nodeType(NodeType nodeType) {
				this.nodeType = nodeType;
				return (B) this;
			}

			public Structure<E> build() {
				return new StructureImpl<>(origin, type, supplier, patterns, priority, entryValidator, nodeType);
			}

			@Override
			public void applyTo(SyntaxInfo.Builder<?, ?> builder) {
				super.applyTo(builder);
				//noinspection rawtypes - Should be safe, generics will not influence this
				if (builder instanceof Structure.Builder structureBuilder) {
					if (entryValidator != null) {
						structureBuilder.entryValidator(entryValidator);
						structureBuilder.nodeType(nodeType);
					}
				}
			}
		}

	}

}
