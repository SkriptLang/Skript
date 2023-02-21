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
package ch.njol.skript.registrations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval;

import com.google.common.collect.ImmutableList;
import ch.njol.skript.Skript;
import org.skriptlang.skript.lang.converter.Converter;
import org.skriptlang.skript.lang.converter.Converters;
import ch.njol.skript.expressions.base.EventValueExpression;

public class EventValues {

	private EventValues() {}

	private final static class EventValueInfo<E extends Event, T> {

		@Nullable
		private final Class<? extends E>[] excludes;

		@Nullable
		private final String excludeErrorMessage;

		private final Converter<E, T> converter;
		private final Class<E> event;
		private final Class<T> c;

		@SafeVarargs
		public EventValueInfo(Class<E> event, Class<T> c, Converter<E, T> converter, @Nullable String excludeErrorMessage, @Nullable Class<? extends E>... excludes) {
			assert converter != null;
			assert event != null;
			assert c != null;

			this.excludeErrorMessage = excludeErrorMessage;
			this.converter = converter;
			this.excludes = excludes;
			this.event = event;
			this.c = c;
		}

		/**
		 * Get the classes that are excluded for this event value.
		 * If the event values are used in any of these events, Skript will error.
		 * Example: command sender or a player in a damage event will error, they should be using attacker/victim.
		 * 
		 * @return The classes of the excluded events for this event value.
		 */
		@SuppressWarnings("unchecked")
		public Class<? extends E>[] getExcludedEvents() {
			if (excludes != null && excludes.length > 0)
				return Arrays.copyOf(excludes, excludes.length);
			return new Class[0];
		}

		/**
		 * Get the error message used when encountering an exclude event.
		 * 
		 * @return The error message to use when encountering an exclude event.
		 */
		@Nullable
		String getExcludeErrorMessage() {
			return excludeErrorMessage;
		}

		/**
		 * Get the converter that will collect the value from the event.
		 * 
		 * @return The converter that will collect the value from the event.
		 */
		Converter<E, T> getConverter() {
			return converter;
		}

		/**
		 * Get the class that represents the Event.
		 * 
		 * @return The class of the Event associated with this event value.
		 */
		Class<E> getEventClass() {
			return event;
		}

		/**
		 * Get the class that represents the value.
		 * 
		 * @return The class of the value associated with this event value.
		 */
		Class<T> getValueClass() {
			return c;
		}

	}

	private final static List<EventValueInfo<?, ?>> defaultEventValues = new ArrayList<>();
	private final static List<EventValueInfo<?, ?>> futureEventValues = new ArrayList<>();
	private final static List<EventValueInfo<?, ?>> pastEventValues = new ArrayList<>();

	/**
	 * The past time of an event value. Represented when using "past" or "was".
	 */
	public static final int TIME_PAST = -1;

	/**
	 * The current time of an event value.
	 */
	public static final int TIME_NOW = 0;

	/**
	 * The future time of an event value. Represented when using "future" or "will be".
	 */
	public static final int TIME_FUTURE = 1;

	/**
	 * Get event values list for the specified time.
	 * 
	 * @param time The time of the event values. One of
	 * 		{@link EventValues#TIME_PAST}, {@link EventValues#TIME_NOW} or {@link EventValues#TIME_FUTURE}.
	 * @return An immutable copy of the event values list for the specified time.
	 */
	public static List<EventValueInfo<?, ?>> getEventValuesListForTime(int time) {
		return ImmutableList.copyOf(getEventValuesList(time));
	}

	private static List<EventValueInfo<?, ?>> getEventValuesList(int time) {
		if (time == TIME_PAST)
			return pastEventValues;
		if (time == TIME_NOW)
			return defaultEventValues;
		if (time == TIME_FUTURE)
			return futureEventValues;
		throw new IllegalArgumentException("time must be " + TIME_PAST + ", " + TIME_NOW + ", or " + TIME_FUTURE);
	}

	/**
	 * Register an event value. Defaults the time state to {@link EventValues#TIME_NOW}.
	 * 
	 * @param event The event class this event value is coming from.
	 * @param c The return type of the event value.
	 * @param converter The converter that will extract the value from the event.
	 */
	public static <T, E extends Event> void registerEventValue(Class<E> event, Class<T> c, Converter<E, T> converter) {
		registerEventValue(event, c, converter, TIME_NOW, null);
	}

	/**
	 * Register an event value.
	 * 
	 * @param event The event class this event value is coming from.
	 * @param c The return type of the event value.
	 * @param converter The converter that will extract the value from the event.
	 * @param time The time state of this event value. Use {@link EventValues#TIME_PAST} if this value was before the event.
	 * 		or use {@link EventValues#TIME_FUTURE} if this value is after the event. {@link EventValues#TIME_NOW} is the default neutral state.
	 * 		If the provided time was not {@link EventValues#TIME_NOW} and its time wasn't found, Skript will default to {@link EventValues#TIME_NOW}.
	 * 		Use {@link EventValues#TIME_NOW} if this event value has no distinct states, and always register a default state {@link EventValues#TIME_NOW} for each return type of the event.
	 */
	public static <T, E extends Event> void registerEventValue(Class<E> event, Class<T> c, Converter<E, T> converter, int time) {
		registerEventValue(event, c, converter, time, null);
	}

	/**
	 * Register an event value with events that should be excluded for this event value.
	 * Some event values you may want to not operate in specific events.
	 * <p>
	 * An example would be Skript's player event value not being allowed in a damage event.
	 * This is because there can be multiple players in a damage event and Skript would be defaulting to
	 * one of the players when the end user may want the other player. So Skript will print an error stating to use victim/attacker.
	 * 
	 * @param event The event class this event value is coming from.
	 * @param c The return type of the event value.
	 * @param converter The converter that will extract the value from the event.
	 * @param time The time state of this event value. Use {@link EventValues#TIME_PAST} if this value was before the event.
	 * 		or use {@link EventValues#TIME_FUTURE} if this value is after the event. {@link EventValues#TIME_NOW} is the default neutral state.
	 * 		If the provided time was not {@link EventValues#TIME_NOW} and its time wasn't found, Skript will default to {@link EventValues#TIME_NOW}.
	 * 		Use {@link EventValues#TIME_NOW} if this event value has no distinct states, and always register a default state {@link EventValues#TIME_NOW} for each return type of the event.
	 * @param excludeErrorMessage The error message to print when an excluded event is used with this event value.
	 * @param excludes Subclasses of the event for which this event value should not be registered for.
	 */
	@SafeVarargs
	public static <T, E extends Event> void registerEventValue(Class<E> event, Class<T> c, Converter<E, T> converter, int time, @Nullable String excludeErrorMessage, @Nullable Class<? extends E>... excludes) {
		Skript.checkAcceptRegistrations();
		List<EventValueInfo<?, ?>> eventValues = getEventValuesList(time);
		for (int i = 0; i < eventValues.size(); i++) {
			EventValueInfo<?, ?> info = eventValues.get(i);
			if (info.getEventClass() != event ? info.getEventClass().isAssignableFrom(event) : info.getValueClass().isAssignableFrom(c)) {
				eventValues.add(i, new EventValueInfo<>(event, c, converter, excludeErrorMessage, excludes));
				return;
			}
		}
		eventValues.add(new EventValueInfo<>(event, c, converter, excludeErrorMessage, excludes));
	}

	/**
	 * Gets a specific value from an event. Returns null if the event doesn't have such a value (conversions are done to try and get the desired value).
	 * <p>
	 * It is recommended to use {@link EventValues#getEventValueGetter(Class, Class, int)} or {@link EventValueExpression#EventValueExpression(Class)} instead of invoking this
	 * method repeatedly.
	 * 
	 * @param event The event to grab the value from.
	 * @param c The return type of the converter.
	 * @param time The time state of this event value. Use {@link EventValues#TIME_PAST} if this value was before the event.
	 * 		or use {@link EventValues#TIME_FUTURE} if this value is after the event. {@link EventValues#TIME_NOW} is the default neutral state.
	 * 		If the provided time was not {@link EventValues#TIME_NOW} and its time wasn't found, Skript will default to {@link EventValues#TIME_NOW}.
	 * @return The value found from possible event values.
	 * @see #registerEventValue(Class, Class, Getter, int)
	 */
	@Nullable
	public static <T, E extends Event> T getEventValue(E event, Class<T> c, int time) {
		@SuppressWarnings("unchecked")
		Converter<? super E, ? extends T> converter = getEventValueConverter((Class<E>) event.getClass(), c, time);
		if (converter == null)
			return null;
		return converter.convert(event);
	}

	/**
	 * @deprecated API name change. Scheduled for removal. Use {@link #getEventValueConverter(Class, Class, int)}
	 */
	@Nullable
	@Deprecated
	@ScheduledForRemoval
	public static <T, E extends Event> Converter<? super E, ? extends T> getEventValueGetter(Class<E> event, Class<T> c, int time) {
		return getEventValueConverter(event, c, time, true);
	}

	/**
	 * Returns a converter to get a value from the defined event. Otherwise null if not possible.
	 * Skript will attempt to convert the type and check subclasses to see if converting is possible.
	 * <p>
	 * Can print an error if the event value is excluded for the given event.
	 * 
	 * @param event The event class the converter will be getting from.
	 * @param c The return type of the converter.
	 * @param time The event-value's time state.
	 * 		If the provided time was not {@link EventValues#TIME_NOW} and its time wasn't found, Skript will default to {@link EventValues#TIME_NOW}.
	 * @return A converter that is used to extract values from the given event class.
	 * @see #registerEventValue(Class, Class, Getter, int)
	 * @see EventValueExpression#EventValueExpression(Class)
	 */
	@Nullable
	public static <T, E extends Event> Converter<? super E, ? extends T> getEventValueConverter(Class<E> event, Class<T> c, int time) {
		return getEventValueConverter(event, c, time, true);
	}

	@Nullable
	@SuppressWarnings("unchecked")
	private static <T, E extends Event> Converter<? super E, ? extends T> getEventValueConverter(Class<E> event, Class<T> c, int time, boolean allowDefault) {
		List<EventValueInfo<?, ?>> eventValues = getEventValuesList(time);

		// First check for exact classes matching the parameters.
		for (EventValueInfo<?, ?> eventValueInfo : eventValues) {
			if (!c.equals(eventValueInfo.getValueClass()))
				continue;
			if (!checkExcludes(eventValueInfo, event))
				return null;
			if (eventValueInfo.getEventClass().isAssignableFrom(event))
				return (Converter<? super E, ? extends T>) eventValueInfo.getConverter();
		}

		// Second check for assignable subclasses.
		for (EventValueInfo<?, ?> eventValueInfo : eventValues) {
			if (!c.isAssignableFrom(eventValueInfo.getValueClass()))
				continue;
			if (!checkExcludes(eventValueInfo, event))
				return null;
			if (eventValueInfo.getEventClass().isAssignableFrom(event))
				return (Converter<? super E, ? extends T>) eventValueInfo.getConverter();
			if (!event.isAssignableFrom(eventValueInfo.getEventClass()))
				continue;
			return new Converter<E, T>() {
				@Override
				@Nullable
				public T convert(E event) {
					if (!eventValueInfo.getEventClass().isInstance(event))
						return null;
					return ((Converter<E, ? extends T>) eventValueInfo.getConverter()).convert(event);
				}
			};
		}

		// Most checks have returned before this below is called, Skript will now attempt to convert or find an alternative.
		// Third check is if the returned object matches the class.
		for (EventValueInfo<?, ?> eventValueInfo : eventValues) {
			if (!eventValueInfo.getValueClass().isAssignableFrom(c))
				continue;
			boolean checkInstanceOf = !eventValueInfo.getEventClass().isAssignableFrom(event);
			if (checkInstanceOf && !event.isAssignableFrom(eventValueInfo.getEventClass()))
				continue;
			if (!checkExcludes(eventValueInfo, event))
				return null;
			return new Converter<E, T>() {
				@Override
				@Nullable
				public T convert(E event) {
					if (checkInstanceOf && !eventValueInfo.getEventClass().isInstance(event))
						return null;
					Object object = ((Converter<? super E, ? super T>) eventValueInfo.getConverter()).convert(event);
					if (c.isInstance(object))
						return (T) object;
					return null;
				}
			};
		}

		// Fourth check will attempt to convert the event value to the requesting type.
		// This first for loop will check that the events are exact. See issue #5016
		for (EventValueInfo<?, ?> eventValueInfo : eventValues) {
			if (!event.equals(eventValueInfo.getEventClass()))
				continue;
			Converter<? super E, ? extends T> converter = (Converter<? super E, ? extends T>) getConvertedConverter(eventValueInfo, c, false);
			if (converter == null)
				continue;
			if (!checkExcludes(eventValueInfo, event))
				return null;
			return converter;
		}

		// This loop will attempt to look for converters assignable to the class of the provided event.
		for (EventValueInfo<?, ?> eventValueInfo : eventValues) {
			// The requesting event must be assignable to the event value's event. Otherwise it'll throw an error.
			if (!event.isAssignableFrom(eventValueInfo.getEventClass()))
				continue;
			Converter<? super E, ? extends T> converter = (Converter<? super E, ? extends T>) getConvertedConverter(eventValueInfo, c, true);
			if (converter == null)
				continue;
			if (!checkExcludes(eventValueInfo, event))
				return null;
			return converter;
		}

		// If the check should try again matching event values with TIME_NOW (most event values) if it's not already.
		if (allowDefault && time != TIME_NOW)
			return getEventValueConverter(event, c, TIME_NOW, false);
		return null;
	}

	/**
	 * Check if the provided event class is excluded in the event value.
	 * 
	 * @param info The event value info that will be used to grab the value from.
	 * @param event The event class to check the excludes against.
	 * @return boolean true if the event value passes for the events.
	 */
	private static boolean checkExcludes(EventValueInfo<?, ?> info, Class<? extends Event> event) {
		if (info.getExcludedEvents() == null)
			return true;
		for (Class<? extends Event> excluded : info.getExcludedEvents()) {
			if (excluded.isAssignableFrom(event)) {
				Skript.error(info.getExcludeErrorMessage());
				return false;
			}
		}
		return true;
	}

	/**
	 * Return a converter wrapped in a converter that will extract the requested value by converting from the given event value info.
	 * 
	 * @param info The event value info that will be used to grab the value from.
	 * @param to The class that the converter will look for to convert the type from the event value to.
	 * @param checkInstanceOf If the event must be an exact instance of the event value info's event or not.
	 * @return A Converter that will convert the event value to the corresponding type, or null if no Converter was possible.
	 */
	@Nullable
	private static <E extends Event, F, T> Converter<? super E, ? extends T> getConvertedConverter(EventValueInfo<E, F> info, Class<T> to, boolean checkInstanceOf) {
		Converter<? super F, ? extends T> converter = Converters.getConverter(info.getValueClass(), to);
		if (converter == null)
			return null;
		return new Converter<E, T>() {
			@Override
			@Nullable
			public T convert(E event) {
				if (checkInstanceOf && !info.getEventClass().isInstance(event))
					return null;
				F value = info.getConverter().convert(event);
				if (value == null)
					return null;
				return converter.convert(value);
			}
		};
	}

	/**
	 * Check if an event has event values in the past or future time states based on the provided return type.
	 * 
	 * @param event The event to check for event values against.
	 * @param c The return type to check against event values for.
	 * @return boolean true if there are event values present of the provided return type and in the future or past time states.
	 */
	public static boolean doesEventValueHaveTimeStates(Class<? extends Event> event, Class<?> c) {
		return getEventValueConverter(event, c, TIME_PAST, false) != null || getEventValueConverter(event, c, TIME_FUTURE, false) != null;
	}

}
