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

import com.google.common.base.MoreObjects;
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
			Collection<String> patterns, Priority priority, Class<R> returnType
		) {
			super(origin, type, supplier, patterns, priority);
			if (returnType.isAnnotation() || returnType.isArray() || returnType.isPrimitive()) {
				throw new IllegalArgumentException("returnType must be a normal type");
			}
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
	public static class StructureImpl<E extends org.skriptlang.skript.lang.structure.Structure>
		extends SyntaxInfoImpl<E> implements DefaultSyntaxInfos.Structure<E> {

		@Nullable
		private final EntryValidator entryValidator;

		StructureImpl(
			SyntaxOrigin origin, Class<E> type, @Nullable Supplier<E> supplier,
			Collection<String> patterns, Priority priority, @Nullable EntryValidator entryValidator
		) {
			super(origin, type, supplier, patterns, priority);
			this.entryValidator = entryValidator;
		}

		@Override
		@Nullable
		public EntryValidator entryValidator() {
			return entryValidator;
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

			@Nullable
			private EntryValidator entryValidator;

			BuilderImpl(Class<E> structureClass) {
				super(structureClass);
			}

			@Override
			public B entryValidator(EntryValidator entryValidator) {
				this.entryValidator = entryValidator;
				return (B) this;
			}

			public Structure<E> build() {
				return new StructureImpl<>(origin, type, supplier, patterns, priority, entryValidator);
			}

		}

	}

}
