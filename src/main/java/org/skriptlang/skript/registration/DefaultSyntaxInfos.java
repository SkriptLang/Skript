package org.skriptlang.skript.registration;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.entry.EntryValidator;
import org.skriptlang.skript.registration.DefaultSyntaxInfosImpl.ExpressionImpl;
import org.skriptlang.skript.registration.DefaultSyntaxInfosImpl.StructureImpl;

/**
 * This class is not safe to be directly referenced.
 * Use {@link SyntaxInfo} instead.
 */
@ApiStatus.Internal
@ApiStatus.Experimental
public interface DefaultSyntaxInfos {

	/**
	 * A syntax info to be used for {@link ch.njol.skript.lang.Expression}s.
	 * It differs from a typical info in that it also has a return type.
	 * @param <E> The class providing the implementation of the Expression this info represents.
	 * @param <R> The type of the return type of the Expression.
	 */
	@ApiStatus.Experimental
	interface Expression<E extends ch.njol.skript.lang.Expression<R>, R> extends SyntaxInfo<E> {

		/**
		 * Constructs a builder for an expression syntax info.
		 * @param expressionClass The Expression class the info will represent.
		 * @param returnType The class representing the supertype of all values the Expression may return.
		 * @return An Expression-specific builder for creating a syntax info representing <code>expressionClass</code>.
		 * @param <E> The class providing the implementation of the Expression this info represents.
		 * @param <R> The supertype of all values the Expression may return.
		 */
		@Contract("_, _ -> new")
		static <E extends ch.njol.skript.lang.Expression<R>, R> Builder<? extends Builder<?, E, R>, E, R> builder(
			Class<E> expressionClass, Class<R> returnType) {
			return new ExpressionImpl.BuilderImpl<>(expressionClass, returnType);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		@Contract("-> new")
		Builder<? extends Builder<?, E, R>, E, R> builder();

		/**
		 * @return The class representing the supertype of all values the Expression may return.
		 */
		Class<R> returnType();

		/**
		 * An Expression-specific builder is used for constructing a new Expression syntax info.
		 * @see #builder(Class)
		 * @param <B> The type of builder being used.
		 * @param <E> The Expression class providing the implementation of the syntax info being built.
		 * @param <R> The type of the return type of the Expression.
		 */
		interface Builder<B extends Builder<B, E, R>, E extends ch.njol.skript.lang.Expression<R>, R> extends SyntaxInfo.Builder<B, E> {

			/**
			 * {@inheritDoc}
			 */
			@Override
			@Contract("-> new")
			Expression<E, R> build();

		}

	}

	/**
	 * A syntax info to be used for {@link org.skriptlang.skript.lang.structure.Structure}s.
	 * It contains additional details including the {@link EntryValidator} to use, if any.
	 * @param <E> The class providing the implementation of the Structure this info represents.
	 */
	@ApiStatus.Experimental
	interface Structure<E extends org.skriptlang.skript.lang.structure.Structure> extends SyntaxInfo<E> {

		/**
		 * Represents type of {@link ch.njol.skript.config.Node}s that can represent a Structure.
		 */
		enum NodeType {

			/**
			 * For Structures that can be represented using a {@link ch.njol.skript.config.SimpleNode}.
			 */
			SIMPLE,

			/**
			 * For Structures that can be represented using a {@link ch.njol.skript.config.SectionNode}.
			 */
			SECTION,

			/**
			 * For Structures that can be represented using a
			 *  {@link ch.njol.skript.config.SimpleNode} or {@link ch.njol.skript.config.SectionNode}.
			 */
			BOTH;

			/**
			 * @return Whether a Structure of this type can be represented using a {@link ch.njol.skript.config.SimpleNode}.
			 */
			public boolean canBeSimple() {
				return this != SECTION;
			}

			/**
			 * @return Whether a Structure of this type can be represented using a {@link ch.njol.skript.config.SectionNode}.
			 */
			public boolean canBeSection() {
				return this != SIMPLE;
			}

		}

		/**
		 * Constructs a builder for a structure syntax info.
		 * @param structureClass The Structure class the info will represent.
		 * @return A Structure-specific builder for creating a syntax info representing <code>structureClass</code>.
		 * By default, the {@link #nodeType()} of the builder is {@link NodeType#SECTION}.
		 * @param <E> The class providing the implementation of the Structure this info represents.
		 */
		@Contract("_ -> new")
		static <E extends org.skriptlang.skript.lang.structure.Structure> Builder<? extends Builder<?, E>, E> builder(Class<E> structureClass) {
			return new StructureImpl.BuilderImpl<>(structureClass);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		@Contract("-> new")
		Builder<? extends Builder<?, E>, E> builder();

		/**
		 * @return The entry validator to use for handling the Structure's entries.
		 *  If null, the Structure is expected to manually handle any entries.
		 */
		@Nullable EntryValidator entryValidator();

		/**
		 * @return The type of {@link ch.njol.skript.config.Node}s that can represent the Structure.
		 */
		NodeType nodeType();

		/**
		 * A Structure-specific builder is used for constructing a new Structure syntax info.
		 * @see #builder(Class)
		 * @param <B> The type of builder being used.
		 * @param <E> The Structure class providing the implementation of the syntax info being built.
		 */
		interface Builder<B extends Builder<B, E>, E extends org.skriptlang.skript.lang.structure.Structure> extends SyntaxInfo.Builder<B, E> {

			/**
			 * Sets the entry validator the Structure will use for handling entries.
			 * @param entryValidator The entry validator to use.
			 * @return This builder.
			 * @see Structure#entryValidator()
			 */
			@Contract("_ -> this")
			B entryValidator(EntryValidator entryValidator);

			/**
			 * Sets the type of {@link ch.njol.skript.config.Node}s that can represent the Structure.
			 * @return This builder.
			 * @see Structure#type()
			 */
			B nodeType(NodeType type);

			/**
			 * {@inheritDoc}
			 */
			@Override
			@Contract("-> new")
			Structure<E> build();

		}

	}

}
