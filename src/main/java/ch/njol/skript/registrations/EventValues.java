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
import java.util.Collections;
import java.util.List;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import ch.njol.skript.Skript;

import org.skriptlang.skript.lang.comparator.Priority;
import org.skriptlang.skript.lang.converter.Converter;
import org.skriptlang.skript.lang.converter.Converters;

import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.util.Getter;

/**
 * The main class for registering and getting anything relating to event values.
 */
public class EventValues {

	private final static class EventValueInfo<E extends Event, T> implements Comparable<EventValueInfo<?, ?>> {

		private final Class<? extends E>[] excludes;

		@Nullable
		private final String excludeErrorMessage;

		private final Getter<T, E> getter;
		private final Class<E> event;
		private final Class<T> type;

		private Priority priority;

		@SuppressWarnings("unchecked")
		public EventValueInfo(Class<E> event, Class<T> type, Getter<T, E> getter, Priority priority, @Nullable String excludeErrorMessage, @Nullable Class<? extends E>... excludes) {
			assert getter != null;
			assert event != null;
			assert type != null;

			this.excludes = excludes == null ? new Class[0] : excludes;
			if (excludes.length > 0 && excludeErrorMessage == null)
				Skript.error("The exclude error message cannot be null when there are excluded events.");

			this.excludeErrorMessage = excludeErrorMessage;
			this.priority = priority;
			this.getter = getter;
			this.event = event;
			this.type = type;
		}

		@Override
		public int compareTo(EventValueInfo<?, ?> other) {
			return priority.compareTo(other.priority);
		}

		@Override
		public String toString() {
			return "(" + event.getSimpleName() + "#" + type.getSimpleName() + " at priority " + priority + ")";
		}

		@Override
		public boolean equals(Object object) {
			if (!(object instanceof EventValueInfo))
				return false;
			EventValueInfo<?, ?> other = (EventValueInfo<?, ?>) object;
			if (!event.equals(other.event))
				return false;
			if (!type.equals(other.type))
				return false;
			if (!getter.equals(other.getter))
				return false;
			if (priority != other.priority)
				return false;
			if (!excludes.equals(other.excludes))
				return false;
			return true;
		}

	}

	private final static List<EventValueInfo<?, ?>> defaultEventValues = new ArrayList<>();
	private final static List<EventValueInfo<?, ?>> futureEventValues = new ArrayList<>();
	private final static List<EventValueInfo<?, ?>> pastEventValues = new ArrayList<>();

	/**
	 * The default priority number.
	 */
	public static final int DEFAULT_PRIORITY = 15;

	/**
	 * The past time of an event value. Represented by "past" or "former".
	 */
	public static final int TIME_PAST = -1;

	/**
	 * The current time of an event value.
	 */
	public static final int TIME_NOW = 0;

	/**
	 * The future time of an event value.
	 */
	public static final int TIME_FUTURE = 1;

	/**
	 * Get Event Values list for the specified time
	 * 
	 * @param time The time of the event values. One of
	 * {@link EventValues#TIME_PAST}, {@link EventValues#TIME_NOW} or {@link EventValues#TIME_FUTURE}.
	 * @return An immutable copy of the event values list for the specified time
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
		throw new IllegalArgumentException("Time must be " + TIME_PAST + ", " + TIME_NOW + " or " + TIME_FUTURE);
	}

	/**
	 * Registers an event value with default priority DEFAULT_PRIORITY and time state at TIME_NOW.
	 * 
	 * @param event the event type class.
	 * @param type the return type of the getter for the event value.
	 * @param getter the getter to get the value with the provided event.
	 */
	public static <T, E extends Event> void registerEventValue(Class<E> event, Class<T> type, Getter<T, E> getter) {
		registerEventValue(event, type, getter, TIME_NOW, Priority.DEFAULT_PRIORITY);
	}

	/**
	 * Registers an event value before other event values and time state at TIME_NOW.
	 * 
	 * @param event the event type class.
	 * @param type the return type of the getter for the event value.
	 * @param getter the getter to get the value with the provided event.
	 * @param before the other event classes to register this event value before.
	 */
	public static <T, E extends Event> void registerEventValue(Class<E> event, Class<T> type, Getter<T, E> getter, Class<? extends E>[] before) {
		registerEventValue(event, type, getter, TIME_NOW, before);
	}

	/**
	 * Registers an event value with default priority DEFAULT_PRIORITY.
	 * 
	 * @param event the event type class.
	 * @param type the return type of the getter for the event value.
	 * @param getter the getter to get the value with the provided event.
	 * @param time value of TIME_PAST if this is the value before the event, TIME_FUTURE if after, and TIME_NOW if it's the default or this value doesn't have distinct states.
	 *            <b>Always register a default state!</b> You can leave out one of the other states instead, e.g. only register a default and a past state. The future state will
	 *            default to the default state in this case.
	 */
	public static <T, E extends Event> void registerEventValue(Class<E> event, Class<T> type, Getter<T, E> getter, int time) {
		registerEventValue(event, type, getter, time, Priority.DEFAULT_PRIORITY);
	}

	/**
	 * Registers an event value.
	 * 
	 * @param event the event type class.
	 * @param type the return type of the getter for the event value.
	 * @param getter the getter to get the value with the provided event.
	 * @param time value of TIME_PAST if this is the value before the event, TIME_FUTURE if after, and TIME_NOW if it's the default or this value doesn't have distinct states.
	 *            <b>Always register a default state!</b> You can leave out one of the other states instead, e.g. only register a default and a past state. The future state will
	 *            default to the default state in this case.
	 * @param priority the priority of this event value compared to other event values when returning as a default expression. 0 is the top of the order. Can be negative.
	 */
	@SuppressWarnings("unchecked")
	public static <T, E extends Event> void registerEventValue(Class<E> event, Class<T> type, Getter<T, E> getter, int time, Priority priority) {
		registerEventValue(event, type, getter, time, priority, null, new Class[0]);
	}

	/**
	 * Registers an event value before other event values.
	 * 
	 * @param event the event type class.
	 * @param type the return type of the getter for the event value.
	 * @param getter the getter to get the value with the provided event.
	 * @param time value of TIME_PAST if this is the value before the event, TIME_FUTURE if after, and TIME_NOW if it's the default or this value doesn't have distinct states.
	 *            <b>Always register a default state!</b> You can leave out one of the other states instead, e.g. only register a default and a past state. The future state will
	 *            default to the default state in this case.
	 * @param after the other event classes to register this event value after.
	 */
	@SuppressWarnings("unchecked")
	public static <T, E extends Event> void registerEventValue(Class<E> event, Class<T> type, Getter<T, E> getter, int time, Class<? extends Event>... after) {
		registerEventValue(event, type, getter, time, after, null, new Class[0]);
	}

	/**
	 * Registers an event value with default priority DEFAULT_PRIORITY and with excluded events.
	 * Excluded events are events that this event value can't operate in.
	 * 
	 * @param event the event type class.
	 * @param type the return type of the getter for the event value.
	 * @param getter the getter to get the value with the provided event.
	 * @param time value of TIME_PAST if this is the value before the event, TIME_FUTURE if after, and TIME_NOW if it's the default or this value doesn't have distinct states.
	 *            <b>Always register a default state!</b> You can leave out one of the other states instead, e.g. only register a default and a past state. The future state will
	 *            default to the default state in this case.
	 * @param excludeErrorMessage The error message to display when used in the excluded events.
	 * @param excludes subclasses of the event for which this event value should not be registered for
	 */
	@SuppressWarnings("unchecked")
	public static <T, E extends Event> void registerEventValue(Class<E> event, Class<T> type, Getter<T, E> getter, int time, @Nullable String excludeErrorMessage, @Nullable Class<? extends E>... excludes) {
		registerEventValue(event, type, getter, time, Priority.DEFAULT_PRIORITY, excludeErrorMessage, excludes);
	}

	/**
	 * Registers an event value before other event values and with excluded events.
	 * Excluded events are events that this event value can't operate in.
	 * 
	 * @param event the event type class.
	 * @param type the return type of the getter for the event value.
	 * @param getter the getter to get the value with the provided event.
	 * @param time value of TIME_PAST if this is the value before the event, TIME_FUTURE if after, and TIME_NOW if it's the default or this value doesn't have distinct states.
	 *            <b>Always register a default state!</b> You can leave out one of the other states instead, e.g. only register a default and a past state. The future state will
	 *            default to the default state in this case.
	 * @param after the other event classes to register this event value after.
	 * @param excludeErrorMessage The error message to display when used in the excluded events.
	 * @param excludes subclasses of the event for which this event value should not be registered for
	 */
	@SuppressWarnings("unchecked")
	public static <T, E extends Event> void registerEventValue(Class<E> event, Class<T> type, Getter<T, E> getter, int time, Class<? extends Event>[] after, @Nullable String excludeErrorMessage, @Nullable Class<? extends E>... excludes) {
		Skript.checkAcceptRegistrations();
		List<EventValueInfo<?, ?>> eventValues = getEventValuesList(time);
		EventValueInfo<E, T> adding = new EventValueInfo<>(event, type, getter, Priority.DEFAULT_PRIORITY, excludeErrorMessage, excludes);
		for (EventValueInfo<?, ?> info : eventValues) {
			// No Duplicates
			if (info.equals(adding))
				return;
			for (Class<? extends Event> other : after) {
				if (!info.event.equals(other))
					continue;
				while (adding.priority.getPriority() < Math.abs(info.priority.getPriority()) && adding.priority.getPriority() != 0)
					adding.priority.increment();
				break;
			}
		}
		eventValues.add(adding);
		Collections.sort(eventValues);
	}

	/**
	 * Registers an event value with default priority DEFAULT_PRIORITY and with excluded events.
	 * Excluded events are events that this event value can't operate in.
	 * 
	 * @param event the event type class.
	 * @param type the return type of the getter for the event value.
	 * @param getter the getter to get the value with the provided event.
	 * @param time value of TIME_PAST if this is the value before the event, TIME_FUTURE if after, and TIME_NOW if it's the default or this value doesn't have distinct states.
	 *            <b>Always register a default state!</b> You can leave out one of the other states instead, e.g. only register a default and a past state. The future state will
	 *            default to the default state in this case.
	 * @param priority the priority of this event value compared to other event values when returning as a default expression. 0 is the top of the order. Can be negative.
	 * @param excludeErrorMessage The error message to display when used in the excluded events.
	 * @param excludes subclasses of the event for which this event value should not be registered for
	 */
	@SuppressWarnings("unchecked")
	public static <T, E extends Event> void registerEventValue(Class<E> event, Class<T> type, Getter<T, E> getter, int time, Priority priority, @Nullable String excludeErrorMessage, @Nullable Class<? extends E>... excludes) {
		Skript.checkAcceptRegistrations();
		List<EventValueInfo<?, ?>> eventValues = getEventValuesList(time);
		EventValueInfo<E, T> adding = new EventValueInfo<>(event, type, getter, priority, excludeErrorMessage, excludes);
		for (EventValueInfo<?, ?> info : eventValues) {
			// No Duplicates
			if (info.equals(adding))
				return;
			// Bump up the priority for the registering event value if it's got something assignable to it already registered.
			if (!info.event.equals(event) ? info.event.isAssignableFrom(event) : info.type.isAssignableFrom(type)) {
				while (Math.abs(adding.priority.getPriority()) >= Math.abs(info.priority.getPriority()) && adding.priority.getPriority() != 0) {
					if (adding.priority.getPriority() < 0)
						adding.priority.increment();
					else
						adding.priority.decrement();
				}
				break;
			}
		}
		eventValues.add(adding);
		Collections.sort(eventValues);
	}

	/**
	 * Gets a specific value from an event. Returns null if the event doesn't have such a value (conversions are done to try and get the desired value).
	 * <p>
	 * It is recommended to use {@link EventValues#getEventValueGetter(Class, Class, int)} or {@link EventValueExpression#EventValueExpression(Class)} instead of invoking this
	 * method repeatedly.
	 * 
	 * @param event the event class the getter will be getting from
	 * @param type return type of getter
	 * @param time value of TIME_PAST if this is the value before the event, TIME_FUTURE if after, and TIME_NOW if it's the default or this value doesn't have distinct states.
	 *            <b>Always register a default state!</b> You can leave out one of the other states instead, e.g. only register a default and a past state. The future state will
	 *            default to the default state in this case.
	 * @return The event's value
	 * @see #registerEventValue(Class, Class, Getter, int)
	 */
	@Nullable
	public static <T, E extends Event> T getEventValue(E event, Class<T> type, int time) {
		@SuppressWarnings("unchecked")
		Getter<? extends T, ? super E> getter = getEventValueGetter((Class<E>) event.getClass(), type, time);
		if (getter == null)
			return null;
		return getter.get(event);
	}

	/**
	 * Checks that a getter exists for the exact type. No converting or subclass checking.
	 * 
	 * @param event the event class the getter will be getting from
	 * @param c type of getter
	 * @param time the event-value's time
	 * @return A getter to get values for a given type of events
	 * @see #registerEventValue(Class, Class, Getter, int)
	 * @see EventValueExpression#EventValueExpression(Class)
	 */
	@Nullable
	@SuppressWarnings("unchecked")
	public static <T, E extends Event> Getter<? extends T, ? super E> getExactEventValueGetter(Class<E> event, Class<T> c, int time) {
		List<EventValueInfo<?, ?>> eventValues = getEventValuesList(time);
		// First check for exact classes matching the parameters.
		for (EventValueInfo<?, ?> eventValueInfo : eventValues) {
			if (!c.equals(eventValueInfo.type))
				continue;
			if (!checkExcludes(eventValueInfo, event))
				return null;
			if (eventValueInfo.event.isAssignableFrom(event))
				return (Getter<? extends T, ? super E>) eventValueInfo.getter;
		}
		return null;
	}

	/**
	 * Returns a getter to get a value from in an event.
	 * <p>
	 * Can print an error if the event value is blocked for the given event.
	 * 
	 * @param event the event class the getter will be getting from
	 * @param type type of getter
	 * @param time the event-value's time
	 * @return A getter to get values for a given type of events
	 * @see #registerEventValue(Class, Class, Getter, int)
	 * @see EventValueExpression#EventValueExpression(Class)
	 */
	@Nullable
	public static <T, E extends Event> Getter<? extends T, ? super E> getEventValueGetter(Class<E> event, Class<T> type, int time) {
		return getEventValueGetter(event, type, time, true);
	}

	@Nullable
	@SuppressWarnings("unchecked")
	private static <T, E extends Event> Getter<? extends T, ? super E> getEventValueGetter(Class<E> event, Class<T> c, int time, boolean allowDefault) {
		List<EventValueInfo<?, ?>> eventValues = getEventValuesList(time);
		// First check for exact classes matching the parameters.
		Getter<? extends T, ? super E> exact = (Getter<? extends T, ? super E>) getExactEventValueGetter(event, c, time);
		if (exact != null)
			return exact;
		// Second check for assignable subclasses.
		for (EventValueInfo<?, ?> eventValueInfo : eventValues) {
			if (!c.isAssignableFrom(eventValueInfo.type))
				continue;
			if (!checkExcludes(eventValueInfo, event))
				return null;
			if (eventValueInfo.event.isAssignableFrom(event))
				return (Getter<? extends T, ? super E>) eventValueInfo.getter;
			if (!event.isAssignableFrom(eventValueInfo.event))
				continue;
			return new Getter<T, E>() {
				@Override
				@Nullable
				public T get(E event) {
					if (!eventValueInfo.event.isInstance(event))
						return null;
					return ((Getter<? extends T, E>) eventValueInfo.getter).get(event);
				}
			};
		}
		// Most checks have returned before this below is called, but Skript will attempt to convert or find an alternative.
		// Third check is if the returned object matches the class.
		for (EventValueInfo<?, ?> eventValueInfo : eventValues) {
			if (!eventValueInfo.type.isAssignableFrom(c))
				continue;
			boolean checkInstanceOf = !eventValueInfo.event.isAssignableFrom(event);
			if (checkInstanceOf && !event.isAssignableFrom(eventValueInfo.event))
				continue;
			if (!checkExcludes(eventValueInfo, event))
				return null;
			return new Getter<T, E>() {
				@Override
				@Nullable
				public T get(E event) {
					if (checkInstanceOf && !eventValueInfo.event.isInstance(event))
						return null;
					Object object = ((Getter<? super T, ? super E>) eventValueInfo.getter).get(event);
					if (c.isInstance(object))
						return (T) object;
					return null;
				}
			};
		}
		// Fourth check will attempt to convert the event value to the requesting type.
		// This first for loop will check that the events are exact. See issue #5016
		for (EventValueInfo<?, ?> eventValueInfo : eventValues) {
			if (!event.equals(eventValueInfo.event))
				continue;
			
			Getter<? extends T, ? super E> getter = (Getter<? extends T, ? super E>) getConvertedGetter(eventValueInfo, c, false);
			if (getter == null)
				continue;
			
			if (!checkExcludes(eventValueInfo, event))
				return null;
			return getter;
		}
		// This loop will attempt to look for converters assignable to the class of the provided event.
		for (EventValueInfo<?, ?> eventValueInfo : eventValues) {
			// The requesting event must be assignable to the event value's event. Otherwise it'll throw an error.
			if (!event.isAssignableFrom(eventValueInfo.event))
				continue;
			
			Getter<? extends T, ? super E> getter = (Getter<? extends T, ? super E>) getConvertedGetter(eventValueInfo, c, true);
			if (getter == null)
				continue;
			
			if (!checkExcludes(eventValueInfo, event))
				return null;
			return getter;
		}
		// If the check should try again matching event values with a 0 time (most event values).
		if (allowDefault && time != 0)
			return getEventValueGetter(event, c, 0, false);
		return null;
	}

	/**
	 * Check if the event value states to exclude events.
	 * 
	 * @param eventValueInfo The event value info that will be used to grab the value from
	 * @param event The event class to check the excludes against.
	 * @return boolean if true the event value passes for the events.
	 */
	private static boolean checkExcludes(EventValueInfo<?, ?> eventValueInfo, Class<? extends Event> event) {
		if (eventValueInfo.excludes == null)
			return true;
		for (Class<? extends Event> ex : (Class<? extends Event>[]) eventValueInfo.excludes) {
			if (ex.isAssignableFrom(event)) {
				Skript.error(eventValueInfo.excludeErrorMessage);
				return false;
			}
		}
		return true;
	}

	/**
	 * Return a converter wrapped in a getter that will grab the requested value by converting from the given event value info.
	 * 
	 * @param eventValueInfo The event value info that will be used to grab the value from
	 * @param to The class that the converter will look for to convert the type from the event value to
	 * @param checkInstanceOf If the event must be an exact instance of the event value info's event or not.
	 * @return The found Converter wrapped in a Getter object, or null if no Converter was found.
	 */
	@Nullable
	private static <E extends Event, F, T> Getter<? extends T, ? super E> getConvertedGetter(EventValueInfo<E, F> eventValueInfo, Class<T> to, boolean checkInstanceOf) {
		Converter<? super F, ? extends T> converter = Converters.getConverter(eventValueInfo.type, to);
		if (converter == null)
			return null;
		return new Getter<T, E>() {
			@Override
			@Nullable
			public T get(E event) {
				if (checkInstanceOf && !eventValueInfo.event.isInstance(event))
					return null;
				F f = eventValueInfo.getter.get(event);
				if (f == null)
					return null;
				return converter.convert(f);
			}
		};
	}

	/**
	 * Checks if an event value from the defined event class and return type has any time states that aren't present.
	 * 
	 * @param event the event to check event values for.
	 * @param type the return type of the event value to search for.
	 * @return boolean if any event values meet the parameters and have a time state that isn't present.
	 */
	public static boolean doesEventValueHaveTimeStates(Class<? extends Event> event, Class<?> type) {
		return getEventValueGetter(event, type, TIME_PAST, false) != null || getEventValueGetter(event, type, TIME_FUTURE, false) != null;
	}

	public static boolean doesExactEventValueHaveTimeStates(Class<? extends Event> event, Class<?> c) {
		return getExactEventValueGetter(event, c, TIME_PAST) != null || getExactEventValueGetter(event, c, TIME_FUTURE) != null;
	}

	/**
	 * Prints a list of registered event values to console if verbose is 'debug'
	 */
	public static void debug() {
		StringBuilder builder = new StringBuilder("All registered event values in order\nPast:");
		for (EventValueInfo<?, ?> eventValueInfo : getEventValuesListForTime(TIME_PAST)) {
			if (builder.length() != 0)
				builder.append(", ");
			builder.append(eventValueInfo);
		}
		builder.append("\n\nPresent:");
		for (EventValueInfo<?, ?> eventValueInfo : getEventValuesListForTime(TIME_NOW)) {
			builder.append(eventValueInfo);
			if (builder.length() != 0)
				builder.append(", ");
		}
		builder.append("\n\nFuture:");
		for (EventValueInfo<?, ?> eventValueInfo : getEventValuesListForTime(TIME_FUTURE)) {
			builder.append(eventValueInfo);
			if (builder.length() != 0)
				builder.append(", ");
		}
		Skript.debug(builder.toString());
	}

}
