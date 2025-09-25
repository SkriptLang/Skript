package org.skriptlang.skript.common.function;

import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an event for a function call.
 *
 * @param <E> The event.
 */
@FunctionalInterface
public interface FunctionEvent<E extends Event> {

	/**
	 * @return The event that called this function event.
	 */
	@NotNull E event();

}
