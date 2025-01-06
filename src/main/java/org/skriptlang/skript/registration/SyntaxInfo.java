package org.skriptlang.skript.registration;

import ch.njol.skript.lang.SyntaxElement;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.registration.SyntaxInfoImpl.BuilderImpl;
import org.skriptlang.skript.util.Priority;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * A syntax info contains the details of a syntax, including its origin and patterns.
 * @param <E> The class providing the implementation of the syntax this info represents.
 */
@ApiStatus.Experimental
public interface SyntaxInfo<E extends SyntaxElement> extends DefaultSyntaxInfos {

	/**
	 * A priority for infos with patterns that only match simple text (they do not have any {@link Expression}s).
	 * Example: "[the] console"
	 */
	Priority SIMPLE = Priority.base();

	/**
	 * A priority for infos with patterns that contain at least one {@link Expression}.
	 * This is typically the default priority of an info.
	 * Example: "[the] first %number% characters of %strings%"
	 */
	Priority COMBINED = Priority.after(SIMPLE);

	/**
	 * A priority for infos with patterns that can match almost anything.
	 * This is likely the case when using regex or multiple expressions next to each other in a pattern.
	 * Example: "[the] [loop-]<.+>"
	 */
	Priority PATTERN_MATCHES_EVERYTHING = Priority.after(COMBINED);

	/**
	 * Constructs a builder for a syntax info.
	 * @param type The syntax class the info will represent.
	 * @return A builder for creating a syntax info representing <code>type</code>.
	 */
	@Contract("_ -> new")
	static <E extends SyntaxElement> Builder<? extends Builder<?, E>, E> builder(Class<E> type) {
		return new BuilderImpl<>(type);
	}

	/**
	 * @return A builder representing this SyntaxInfo.
	 */
	@Contract("-> new")
	Builder<? extends Builder<?, E>, E> toBuilder();

	/**
	 * @return The origin of this syntax.
	 */
	SyntaxOrigin origin();

	/**
	 * @return The class providing the implementation of this syntax.
	 */
	Class<E> type();

	/**
	 * @return A new instance of the class providing the implementation of this syntax.
	 */
	@Contract("-> new")
	E instance();

	/**
	 * @return The patterns of this syntax.
	 */
	@Unmodifiable Collection<String> patterns();

	/**
	 * @return The priority of this syntax, which dictates its position for matching during parsing.
	 */
	Priority priority();

	/**
	 * A builder is used for constructing a new syntax info.
	 * @see #builder(Class)
	 * @param <B> The type of builder being used.
	 * @param <E> The class providing the implementation of the syntax info being built.
	 */
	interface Builder<B extends Builder<B, E>, E extends SyntaxElement> {

		/**
		 * Sets the origin the syntax info will use.
		 * @param origin The origin to use.
		 * @return This builder.
		 * @see SyntaxInfo#origin()
		 */
		@Contract("_ -> this")
		B origin(SyntaxOrigin origin);

		/**
		 * Sets the supplier the syntax info will use to create new instances of the implementing class.
		 * @param supplier The supplier to use.
		 * @return This builder.
		 * @see SyntaxInfo#instance()
		 */
		@Contract("_ -> this")
		B supplier(Supplier<E> supplier);

		/**
		 * Adds a new pattern to the syntax info.
		 * @param pattern The pattern to add.
		 * @return This builder.
		 * @see SyntaxInfo#patterns()
		 */
		@Contract("_ -> this")
		B addPattern(String pattern);

		/**
		 * Adds new patterns to the syntax info.
		 * @param patterns The patterns to add.
		 * @return This builder.
		 * @see SyntaxInfo#patterns()
		 */
		@Contract("_ -> this")
		B addPatterns(String... patterns);

		/**
		 * Adds new patterns to the syntax info.
		 * @param patterns The patterns to add.
		 * @return This builder.
		 * @see SyntaxInfo#patterns()
		 */
		@Contract("_ -> this")
		B addPatterns(Collection<String> patterns);

		/**
		 * Removes all patterns from the syntax info.
		 * @return This builder.
		 * @see SyntaxInfo#patterns()
		 */
		@Contract("-> this")
		B clearPatterns();

		/**
		 * Sets the priority the syntax info will use, which dictates its position for matching during parsing.
		 * @param priority The priority to use.
		 * @return This builder.
		 */
		@Contract("_ -> this")
		B priority(Priority priority);

		/**
		 * Builds a new syntax info from the set details.
		 * @return A syntax info representing the class providing the syntax's implementation.
		 */
		@Contract("-> new")
		SyntaxInfo<E> build();

		/**
		 * Applies the values of this builder onto <code>builder</code>.
		 * When using this method, it is possible that <b>some values are not safe to copy over</b>.
		 * For example, when applying a SyntaxInfo for some type to a SyntaxInfo of another type,
		 * it is *not* safe to copy over {@link #supplier(Supplier)}, but that operation will occur anyway.
		 * In cases like this, you are expected to correct the values.
		 * @param builder The builder to apply values onto.
		 */
		void applyTo(Builder<?, ?> builder);

	}

}
