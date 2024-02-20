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
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.lang.Priority;
import org.skriptlang.skript.registration.SyntaxInfoImpl.BuilderImpl;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * A syntax info contains the details of a syntax, including its origin and patterns.
 * @param <E> The class providing the implementation of the syntax this info represents.
 */
@ApiStatus.Experimental
public interface SyntaxInfo<E extends SyntaxElement> extends DefaultSyntaxInfos {

	/**
	 * @param type The syntax class the info will represent.
	 * @return A builder for creating a syntax info representing <code>type</code>.
	 */
	@Contract("_ -> new")
	static <E extends SyntaxElement> Builder<? extends Builder<?, E>, E> builder(Class<E> type) {
		return new BuilderImpl<>(type);
	}

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
	@Unmodifiable
	Collection<String> patterns();

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
		 * Adds a new pattern for the syntax info.
		 * @param pattern The pattern to add.
		 * @return This builder.
		 * @see SyntaxInfo#patterns()
		 */
		@Contract("_ -> this")
		B addPattern(String pattern);

		/**
		 * Adds new patterns for the syntax info.
		 * @param patterns The patterns to add.
		 * @return This builder.
		 * @see SyntaxInfo#patterns()
		 */
		@Contract("_ -> this")
		B addPatterns(String... patterns);

		/**
		 * Adds new patterns for the syntax info.
		 * @param patterns The patterns to add.
		 * @return This builder.
		 * @see SyntaxInfo#patterns()
		 */
		@Contract("_ -> this")
		B addPatterns(Collection<String> patterns);

		/**
		 * Builds a new syntax info from the set details.
		 * @return A syntax info representing the class providing the syntax's implementation.
		 */
		@Contract("-> new")
		SyntaxInfo<E> build();

	}

}
