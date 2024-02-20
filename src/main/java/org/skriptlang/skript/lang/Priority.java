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
package org.skriptlang.skript.lang;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Priorities are used to determine the order in which syntax elements should be loaded.
 * As the priority approaches 0, it becomes more important. Example:
 * priority of 1 (loads first), priority of 2 (loads second), priority of 3 (loads third)
 */
@ApiStatus.NonExtendable
public class Priority implements Comparable<Priority> {

	/**
	 * The default {@link Priority}.
	 */
	public static final Priority DEFAULT_PRIORITY = new Priority(1000);
	private static final AtomicInteger iota = new AtomicInteger();

	private final int priority;

	public Priority() {
		this.priority = iota.getAndIncrement();
	}

	public Priority(@Range(from = 0, to = Integer.MAX_VALUE) int priority) {
		this.priority = priority;
	}

	public int getPriority() {
		return priority;
	}

	@Override
	public int compareTo(@NotNull Priority other) {
		return Integer.compare(priority, other.priority);
	}

	@Override
	public boolean equals(Object other) {
		if (this == other)
			return true;

		if (other == null || getClass() != other.getClass())
			return false;

		return getPriority() == ((Priority) other).getPriority();
	}

	@Override
	public int hashCode() {
		return getPriority();
	}

}
