package ch.njol.skript.lang;

import ch.njol.skript.lang.util.SimpleExpression;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public interface Loopable<T> {

	/**
	 * Returns the same as {@link Expression#getArray(Event)} but as an iterator. This method should be overridden by expressions intended to be looped to increase performance.
	 * @see SimpleExpression#iterator(Event)
	 *
	 * @param event The event to be used for evaluation
	 * @return An iterator to iterate over all values of this expression which may be empty and/or null, but must not return null elements.
	 */
	@Nullable Iterator<? extends T> iterator(Event event);

	/**
	 * Checks whether the given 'loop-...' expression should match this loop, e.g. loop-block matches any loops that loop through blocks and loop-argument matches an
	 * argument loop.
	 * <p>
	 * You should usually just return false as e.g. loop-block will automatically match the expression if its returnType is Block or a subtype of it.
	 *
	 * @param input The entered input string (the blank in loop-___)
	 * @return Whether this loop matches the given string
	 */
	boolean isLoopOf(String input);

	/**
	 * Checks whether the expression allows using 'next loop-value' when provided as the iterator for a SecLoop.
	 * @return
	 */
	default boolean supportsLoopPeeking() {
		return false;
	};

}
