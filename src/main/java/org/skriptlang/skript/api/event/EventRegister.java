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
package org.skriptlang.skript.api.event;

import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * EventRegisters are generic containers for registering events.
 * They are used across Skript to provide additional API with consistent organization.
 * @param <E> The umbrella class representing the type of events this register will hold.
 */
public class EventRegister<E> {

	private final Set<E> events = new HashSet<>(5);

	/**
	 * Registers the provided event with this register.
	 * @param event The event to register.
	 */
	public void registerEvent(E event) {
		events.add(event);
	}

	/**
	 * Registers the provided event with this register.
	 * @param eventType The type of event being register. This is useful for registering the event using a lambda.
	 * @param event The event to register.
	 */
	public <T extends E> void registerEvent(Class<T> eventType, T event) {
		events.add(event);
	}

	/**
	 * Unregisters the provided event with this Script.
	 * @param event The event to unregister.
	 */
	public void unregisterEvent(E event) {
		events.remove(event);
	}

	/**
	 * @return An unmodifiable set of this register's events.
	 */
	@Unmodifiable
	public Set<E> getEvents() {
		return Collections.unmodifiableSet(events);
	}

	/**
	 * @param type The type of events to get.
	 * @return An unmodifiable set of this register's events of the specified type.
	 */
	@Unmodifiable
	@SuppressWarnings("unchecked")
	public <T extends E> Set<T> getEvents(Class<T> type) {
		return Collections.unmodifiableSet(
			(Set<T>) events.stream()
				.filter(event -> type.isAssignableFrom(event.getClass()))
				.collect(Collectors.toSet())
		);
	}
	
}
