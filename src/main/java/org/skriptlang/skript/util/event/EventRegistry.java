package org.skriptlang.skript.util.event;

import com.google.common.collect.ImmutableSet;
import org.jetbrains.annotations.Unmodifiable;

import java.util.HashSet;
import java.util.Set;

/**
 * An EventRegistry is a generic container for events.
 * They are to be used for providing standardized Event functionality wherever deemed useful.
 * @param <E> The class representing the type of events this register will hold.
 */
public class EventRegistry<E extends Event> {

	private final Set<E> events = new HashSet<>();

	/**
	 * Registers the provided event with this register.
	 * @param event The event to register.
	 */
	public void register(E event) {
		events.add(event);
	}

	/**
	 * Registers the provided event with.
	 * @param eventType The type of event being registered.
	 *  This is useful for registering an event that is a {@link FunctionalInterface} using a lambda.
	 * @param event The event to register.
	 */
	public <T extends E> void register(Class<T> eventType, T event) {
		events.add(event);
	}

	/**
	 * Unregisters the provided event.
	 * @param event The event to unregister.
	 */
	public void unregister(E event) {
		events.remove(event);
	}

	/**
	 * @return An unmodifiable set of this register's events.
	 */
	public @Unmodifiable Set<E> events() {
		return ImmutableSet.copyOf(events);
	}

	/**
	 * @param type The type of events to get.
	 * @return An unmodifiable subset (of the specified type) of this register's events
	 */
	@SuppressWarnings("unchecked")
	public <T extends E> @Unmodifiable Set<T> events(Class<T> type) {
		ImmutableSet.Builder<T> builder = ImmutableSet.builder();
		events.stream()
			.filter(event -> type.isAssignableFrom(event.getClass()))
			.forEach(e -> builder.add((T) e));
		return builder.build();
	}
	
}
