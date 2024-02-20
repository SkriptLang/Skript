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

import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Section;
import ch.njol.skript.lang.Statement;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.UnmodifiableView;
import org.skriptlang.skript.lang.structure.Structure;
import org.skriptlang.skript.registration.SyntaxRegistryImpl.ChildKeyImpl;
import org.skriptlang.skript.registration.SyntaxRegistryImpl.UnmodifiableRegistry;

import java.util.Collection;

/**
 * A syntax registry manages all {@link SyntaxRegister}s for syntax registration.
 */
@ApiStatus.Experimental
public interface SyntaxRegistry {

	/**
	 * A key representing the built-in {@link Structure} syntax element.
	 */
	Key<SyntaxInfo.Structure<?>> STRUCTURE = Key.of("structure");

	/**
	 * A key representing the built-in {@link Section} syntax element.
	 */
	Key<SyntaxInfo<? extends Section>> SECTION = Key.of("section");

	/**
	 * A key representing all {@link Statement} syntax elements.
	 * By default, this includes {@link #EFFECT} and {@link #CONDITION}.
	 */
	Key<SyntaxInfo<? extends Statement>> STATEMENT = Key.of("statement");

	/**
	 * A key representing the built-in {@link Effect} syntax element.
	 */
	Key<SyntaxInfo<? extends Effect>> EFFECT = ChildKey.of(STATEMENT, "effect");

	/**
	 * A key representing the built-in {@link Condition} syntax element.
	 */
	Key<SyntaxInfo<? extends Condition>> CONDITION = ChildKey.of(STATEMENT, "condition");

	/**
	 * A key representing the built-in {@link Expression} syntax element.
	 */
	Key<SyntaxInfo.Expression<?, ?>> EXPRESSION = Key.of("expression");

	/**
	 * This implementation is practically a wrapper around {@code Map<Key<?>, SyntaxRegistry<?>>}.
	 * @return A default registry implementation.
	 */
	@Contract("-> new")
	static SyntaxRegistry createInstance() {
		return new SyntaxRegistryImpl();
	}

	/**
	 * @param registry The registry backing this unmodifiable view.
	 * @return An unmodifiable view of <code>registry</code>.
	 */
	@Contract("_ -> new")
	@UnmodifiableView
	static SyntaxRegistry unmodifiableView(SyntaxRegistry registry) {
		return new UnmodifiableRegistry(registry);
	}

	/**
	 * @param key The key to obtain syntaxes from.
	 * @return An unmodifiable snapshot of all syntaxes registered under <code>key</code>.
	 * @param <I> The syntax type.
	 */
	@Unmodifiable
	<I extends SyntaxInfo<?>> Collection<I> syntaxes(Key<I> key);

	/**
	 * Registers a new syntax under a provided key.
	 *
	 * @param key The key to register <code>info</code> under.
	 * @param info The syntax info to register.
	 * @param <I> The syntax type.
	 */
	<I extends SyntaxInfo<?>> void register(Key<I> key, I info);

	/**
	 * Represents a syntax element type.
	 * @param <I> The syntax type.
	 */
	interface Key<I extends SyntaxInfo<?>> {

		/**
		 * @param name The name of this key.
		 * @return A default key implementation.
		 * @param <I> The syntax type.
		 */
		@Contract("_ -> new")
		static <I extends SyntaxInfo<?>> Key<I> of(String name) {
			return new SyntaxRegistryImpl.KeyImpl<>(name);
		}

		/**
		 * @return The name of the syntax element this key represents.
		 */
		String name();

	}

	/**
	 * Like a {@link Key}, but it has a parent which causes elements to be registered to itself and its parent.
	 * @param <I> The child key's syntax type.
	 * @param <P> The parent key's syntax type.
	 */
	interface ChildKey<I extends P, P extends SyntaxInfo<?>> extends Key<I> {

		/**
		 * @param parent The parent of this key.
		 * @param name The name of this key.
		 * @return A default child key implementation.
		 * @param <I> The child key's syntax type.
		 * @param <P> The parent key's syntax type.
		 */
		@Contract("_, _ -> new")
		static <I extends P, P extends SyntaxInfo<?>> Key<I> of(Key<P> parent, String name) {
			return new ChildKeyImpl<>(parent, name);
		}

		/**
		 * @return The parent key of this child key.
		 */
		Key<P> parent();

	}

}
