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
	public static class ExpressionImpl<E extends ch.njol.skript.lang.Expression<R>, R>
		extends SyntaxInfoImpl<E> implements DefaultSyntaxInfos.Expression<E, R> {

		private final Class<R> returnType;

		ExpressionImpl(
			SyntaxOrigin origin, Class<E> type, @Nullable Supplier<E> supplier,
			Collection<String> patterns, Priority priority, @Nullable Class<R> returnType
		) {
			super(origin, type, supplier, patterns, priority);
			Preconditions.checkArgument(returnType != null, "An expression syntax info must have a return type.");
			this.returnType = returnType;
		}

		@Override
		public Class<R> returnType() {
			return returnType;
		}

		@Override
		public boolean equals(Object other) {
			if (!(other instanceof Expression) || !super.equals(other)) {
				return false;
			}
			ExpressionImpl<?, ?> expression = (ExpressionImpl<?, ?>) other;
			return returnType() == expression.returnType();
		}

		@Override
		public int hashCode() {
			return Objects.hash(origin(), type(), patterns(), returnType());
		}

		@Override
		public String toString() {
			return MoreObjects.toStringHelper(this)
					.add("origin", origin())
					.add("type", type())
					.add("patterns", patterns())
					.add("returnType", returnType())
					.toString();
		}

		/**
		 * {@inheritDoc}
		 */
		@SuppressWarnings("unchecked")
		static final class BuilderImpl<B extends Expression.Builder<B, E, R>, E extends ch.njol.skript.lang.Expression<R>, R>
			extends SyntaxInfoImpl.BuilderImpl<B, E>
			implements Expression.Builder<B, E, R> {

			private @Nullable Class<R> returnType;

			BuilderImpl(Class<E> expressionClass) {
				super(expressionClass);
			}

			@Override
			public B returnType(Class<R> returnType) {
				this.returnType = returnType;
				return (B) this;
			}

			public Expression<E, R> build() {
				return new ExpressionImpl<>(origin, type, supplier, patterns, priority, returnType);
			}

		}

	}

	/**
	 * {@inheritDoc}
	 */
	public static class StructureImpl<E extends org.skriptlang.skript.lang.structure.Structure>
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
		public @Nullable EntryValidator entryValidator() {
			return entryValidator;
		}

		@Override
		public NodeType nodeType() {
			return nodeType;
		}

		@Override
		public boolean equals(Object other) {
			if (!(other instanceof Structure) || !super.equals(other)) {
				return false;
			}
			Structure<?> structure = (Structure<?>) other;
			return Objects.equals(entryValidator(), structure.entryValidator());
		}

		@Override
		public int hashCode() {
			return Objects.hash(origin(), type(), patterns(), entryValidator());
		}

		@Override
		public String toString() {
			return MoreObjects.toStringHelper(this)
					.add("origin", origin())
					.add("type", type())
					.add("patterns", patterns())
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

		}

	}

}
