package org.skriptlang.skript.util;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;

/**
 * Priorities are used for things like ordering syntax and loading structures in a specific order.
 */
@ApiStatus.Experimental
public interface Priority extends Comparable<Priority> {

	/**
	 * @return A base priority for other priorities to build relationships off of.
	 */
	@Contract("-> new")
	static Priority base() {
		return new PriorityImpl();
	}

	/**
	 * Constructs a new priority that is before <code>priority</code>.
	 * Note that this method will not make any changes to the {@link #after()} of <code>priority</code>.
	 * @param priority The priority that will be after the returned priority.
	 * @return A priority that is before <code>priority</code>.
	 */
	@Contract("_ -> new")
	static Priority before(Priority priority) {
		return new PriorityImpl(priority, true);
	}

	/**
	 * Constructs a new priority that is after <code>priority</code>.
	 * Note that this method will not make any changes to the {@link #before()} of <code>priority</code>.
	 * @param priority The priority that will be before the returned priority.
	 * @return A priority that is after <code>priority</code>.
	 */
	@Contract("_ -> new")
	static Priority after(Priority priority) {
		return new PriorityImpl(priority, false);
	}

	/**
	 * @return A collection of all priorities this priority is known to be after.
	 */
	@Unmodifiable Collection<Priority> after();

	/**
	 * @return A collection of all priorities this priority is known to be before.
	 */
	@Unmodifiable Collection<Priority> before();

}
