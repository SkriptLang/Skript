package ch.njol.skript.registrations;

import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.Converter;

/**
 * Used as a converter in EventValue registration to allow for setting event-values
 *
 * @param <E> Event class to change value
 * @param <T> Type of value to change
 */
public interface EventConverter<E extends Event, T> extends Converter<E, T> {

	/**
	 * Set the value of something in an event
	 *
	 * @param event Event to have value changed
	 * @param value Value to change in event
	 */
	void set(E event, @Nullable T value);

}
