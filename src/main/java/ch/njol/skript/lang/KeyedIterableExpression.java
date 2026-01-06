package ch.njol.skript.lang;

import org.bukkit.event.Event;

import java.util.Iterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface KeyedIterableExpression<T> extends Expression<T> {

	/**
	 * Checks whether this expression can provide keys when iterated.
	 *
	 * @return true if this expression can provide keys, false otherwise
	 */
	boolean canIterateWithKeys();

	/**
	 * Returns an iterator over the keyed values of this expression.
	 * <br/>
	 * This should <b>only</b> be called iff {@link #canIterateWithKeys()} returns {@code true}.
	 *
	 * @param event The event context
	 * @return An iterator over the key-value pairs of this expression
	 */
	Iterator<KeyedValue<T>> keyedIterator(Event event);

	default Stream<KeyedValue<T>> keyedStream(Event event) {
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(keyedIterator(event), 0), false);
	}

	@Override
	default boolean isLoopOf(String input) {
		return canIterateWithKeys() && isIndexLoop(input);
	}

	/**
	 * Checks whether the 'loop-...' expression should match this loop's index,
	 * e.g. loop-index matches the index of a loop that iterates over a list variable.
	 *
	 * @param input the input to check
	 * @return true if the input matches the index loop, false otherwise
	 */
	default boolean isIndexLoop(String input) {
		return input.equalsIgnoreCase("index");
	}

	/**
	 * Checks if the given expression can be iterated with keys.
	 *
	 * @param expression the expression to check
	 * @return true if the expression can be iterated with keys, false otherwise
	 * @see #canIterateWithKeys()
	 */
	static boolean canIterateWithKeys(Expression<?> expression) {
		return expression instanceof KeyedIterableExpression<?> keyed && keyed.canIterateWithKeys();
	}

}
