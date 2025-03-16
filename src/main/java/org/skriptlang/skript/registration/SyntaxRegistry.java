package org.skriptlang.skript.registration;

import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Section;
import ch.njol.skript.lang.Statement;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.lang.structure.Structure;
import org.skriptlang.skript.util.Registry;
import org.skriptlang.skript.util.ViewProvider;

import java.util.Collection;

/**
 * A syntax registry manages all {@link SyntaxRegister}s for syntax registration.
 */
@ApiStatus.Experimental
public interface SyntaxRegistry extends ViewProvider<SyntaxRegistry>, Registry<SyntaxInfo<?>> {

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
	 * Constructs a default implementation of a syntax registry.
	 * This implementation is practically a wrapper around {@code Map<Key<?>, SyntaxRegistry<?>>}.
	 * @return A syntax registry containing no elements.
	 */
	@Contract("-> new")
	static SyntaxRegistry empty() {
		return new SyntaxRegistryImpl();
	}

	/**
	 * A method to obtain all syntaxes registered under a certain key.
	 * @param key The key to obtain syntaxes from.
	 * @return An unmodifiable snapshot of all syntaxes registered under <code>key</code>.
	 * @param <I> The syntax type.
	 */
	<I extends SyntaxInfo<?>> @Unmodifiable Collection<I> syntaxes(Key<I> key);

	/**
	 * Registers a new syntax under a provided key.
	 * @param key The key to register <code>info</code> under.
	 * @param info The syntax info to register.
	 * @param <I> The syntax type.
	 */
	<I extends SyntaxInfo<?>> void register(Key<I> key, I info);

	/**
	 * Unregisters a syntax registered under a provided key.
	 * @param key The key the <code>info</code> is registered under.
	 * @param info The syntax info to unregister.
	 * @param <I> The syntax type.
	 */
	<I extends SyntaxInfo<?>> void unregister(Key<I> key, I info);

	/**
	 * Constructs an unmodifiable view of this syntax registry.
	 * That is, the returned registry will not allow registrations.
	 * @return An unmodifiable view of this syntax registry.
	 */
	@Override
	@Contract("-> new")
	default SyntaxRegistry unmodifiableView() {
		return new SyntaxRegistryImpl.UnmodifiableRegistry(this);
	}

	/**
	 * {@inheritDoc}
	 * There are no guarantees on the ordering of the returned collection.
	 * @return An unmodifiable snapshot of all syntaxes registered.
	 */
	@Override
	@Unmodifiable Collection<SyntaxInfo<?>> elements();

	/**
	 * Represents a syntax element type.
	 * @param <I> The syntax type.
	 */
	@ApiStatus.Experimental
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
	@ApiStatus.Experimental
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
			return new SyntaxRegistryImpl.ChildKeyImpl<>(parent, name);
		}

		/**
		 * @return The parent key of this child key.
		 */
		Key<P> parent();

	}

}
