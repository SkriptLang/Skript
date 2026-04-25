package org.skriptlang.skript.registration;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.docs.Origin;
import org.skriptlang.skript.lang.entry.EntryValidator;
import org.skriptlang.skript.util.Priority;

import java.util.Objects;
import java.util.SequencedCollection;
import java.util.function.Supplier;

final class DefaultSyntaxInfosImpl {

	/**
	 * {@inheritDoc}
	 */
	static final class ExpressionImpl<E extends ch.njol.skript.lang.Expression<R>, R>
		extends SyntaxInfoImpl<E> implements DefaultSyntaxInfos.Expression<E, R> {

		private final Class<R> returnType;

		ExpressionImpl(
			Origin origin, Class<E> type, @Nullable Supplier<E> supplier,
			SequencedCollection<String> patterns, Priority priority, @Nullable Class<R> returnType
		) {
			super(origin, type, supplier, patterns, priority);
			Preconditions.checkNotNull(returnType, "An expression syntax info must have a return type.");
			this.returnType = returnType;
		}

		@Override
		public Expression.Builder<? extends Expression.Builder<?, E, R>, E, R> toBuilder() {
			var builder = new BuilderImpl<>(type(), returnType);
			super.toBuilder().applyTo(builder);
			return builder;
		}

		@Override
		public Class<R> returnType() {
			return returnType;
		}

		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (!(other instanceof Expression<?, ?> info)) {
				return false;
			}
			// if 'other' is a custom implementation, have it compare against this to ensure symmetry
			if (other.getClass() != ExpressionImpl.class && !other.equals(this)) {
				return false;
			}
			// compare known data
			return type() == info.type() &&
				Objects.equals(patterns(), info.patterns()) &&
				Objects.equals(priority(), info.priority()) &&
				returnType() == info.returnType();
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
	static final class StructureImpl<E extends org.skriptlang.skript.lang.structure.Structure>
		extends SyntaxInfoImpl<E> implements DefaultSyntaxInfos.Structure<E> {

		private final @Nullable EntryValidator entryValidator;
		private final NodeType nodeType;

		StructureImpl(
			Origin origin, Class<E> type, @Nullable Supplier<E> supplier,
			SequencedCollection<String> patterns, Priority priority,
			@Nullable EntryValidator entryValidator, NodeType nodeType
		) {
			super(origin, type, supplier, patterns, priority);
			if (!nodeType.canBeSection() && entryValidator != null)
				throw new IllegalArgumentException("Simple Structures cannot have an EntryValidator");
			this.entryValidator = entryValidator;
			this.nodeType = nodeType;
		}

		@Override
		public Structure.Builder<? extends Structure.Builder<?, E>, E> toBuilder() {
			var builder = new BuilderImpl<>(type());
			super.toBuilder().applyTo(builder);
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
			if (this == other) {
				return true;
			}
			if (!(other instanceof Structure<?> info)) {
				return false;
			}
			// if 'other' is a custom implementation, have it compare against this to ensure symmetry
			if (other.getClass() != StructureImpl.class && !other.equals(this)) {
				return false;
			}
			// compare known data
			return type() == info.type() &&
				Objects.equals(patterns(), info.patterns()) &&
				Objects.equals(priority(), info.priority()) &&
				Objects.equals(entryValidator(), info.entryValidator()) &&
				nodeType() == info.nodeType();
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
				if (builder instanceof Structure.Builder<?, ?> structureBuilder) {
					if (entryValidator != null) {
						structureBuilder.entryValidator(entryValidator);
					}
					structureBuilder.nodeType(nodeType);
				}
			}
		}

	}

}
