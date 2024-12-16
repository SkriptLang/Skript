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
	void change(Event event, Object @NotNull [] delta, ChangeMode mode, @NotNull String @NotNull [] keys);

}
