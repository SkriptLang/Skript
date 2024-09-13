package ch.njol.skript.lang;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.Changer.ChangeMode;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an expression that is able to accept a set of keys linked to values
 * during the {@link ChangeMode#SET} {@link Changer}.
 *
 * @see Expression
 * @see KeyReceiverExpression
 */
public interface KeyReceiverExpression<T> extends Expression<T> {

	/**
	 * An alternative changer method that provides a set of keys as well as a set of values.
	 * This is only ever called for {@link ChangeMode#supportsKeyedChange()} safe change modes,
	 * where a set of values is provided.
	 * (This will never be called for valueless {@link ChangeMode#DELETE} or {@link ChangeMode#RESET} changers,
	 * for example.)
	 *
	 * @param event The current event context
	 * @param delta The change values
	 * @param mode  The key-safe change mode {@link ChangeMode#SET}
	 * @param keys  The keys, matching the length and order of the values array
	 */
	default void change(Event event, Object @NotNull [] delta, ChangeMode mode, @NotNull String @NotNull [] keys) {
		assert delta.length == keys.length;
		this.change(event, delta, mode);
	}

	/**
	 * An alternative changer method that offers the key-providing expression.
	 * This is only ever called for {@link ChangeMode#supportsKeyedChange()} safe change modes,
	 * where a set of values is provided.
	 * (This will never be called for valueless {@link ChangeMode#DELETE} or {@link ChangeMode#RESET} changers,
	 * for example.)
	 * This also includes a note from the {@link KeyProviderExpression} about whether the keys are 'recommended'
	 * to be used. (I.e. if we don't <i>need</i> the keys, should we still use them?)
	 * <br/>
	 * <br/>
	 * By default, this will fall back to the regular {@link #change(Event, Object[], ChangeMode)} method
	 * if keys are not recommended
	 *
	 * @param event       The current event context
	 * @param delta       The change values
	 * @param mode        The key-safe change mode {@link ChangeMode#SET}
	 * @param keys        The keys, matching the length and order of the values array
	 * @param recommended Whether the source expression wants us to use the keys
	 */
	default void change(Event event, Object @NotNull [] delta, ChangeMode mode, @NotNull String @NotNull [] keys,
						boolean recommended) {
		if (recommended)
			this.change(event, delta, mode, keys);
		else
			this.change(event, delta, mode);
	}

}
