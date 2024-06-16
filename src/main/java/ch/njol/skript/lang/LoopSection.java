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
package ch.njol.skript.lang;

import org.bukkit.event.Event;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Represents a loop section.
 * 
 * @see ch.njol.skript.sections.SecWhile
 * @see ch.njol.skript.sections.SecLoop
 */
public abstract class LoopSection extends Section implements SyntaxElement, Debuggable {

	protected final transient Map<Event, Long> currentLoopCounter = new WeakHashMap<>();

	/**
	 * @param event The event where the loop is used to return its loop iterations
	 * @return The loop iteration number
	 */
	public long getLoopCounter(Event event) {
		return currentLoopCounter.getOrDefault(event, 1L);
	}

	/**
	 * @return The next {@link TriggerItem} after the loop
	 */
	public abstract TriggerItem getActualNext();

	/**
	 * Exit the loop, used to reset the loop properties such as iterations counter
	 * @param event The event where the loop is used to reset its relevant properties
	 */
	public void exit(Event event) {
		currentLoopCounter.remove(event);
	}

}
