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

import com.google.common.collect.ImmutableList;
import ch.njol.skript.Skript;
import ch.njol.skript.classes.Converter;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.util.Getter;

public class EventValues {

	private EventValues() {}

	private static class EventValueInfo<E extends Event, T> {

		@Nullable
		private final Class<? extends Event>[] excludes;

		@Nullable
		private final String excludeErrorMessage;

		private final Converter<E, T> converter;
		private final Class<E> event;
		private final Class<T> c;

		public EventValueInfo(Class<E> event, Class<T> c, Converter<E, T> converter, @Nullable String excludeErrorMessage, @Nullable Class<? extends Event>[] excludes) {
			assert event != null;
			assert c != null;
			assert converter != null;
			this.event = event;
			this.c = c;
			this.converter = converter;
			this.excludes = excludes;
			this.excludeErrorMessage = excludeErrorMessage;
		}

		/**
		 * Return the converter that is used to get the value from the event.
		 * 
		 * @return the converter that is used to get the value from the event.
		 */
		public Converter<E, T> getConverter() {
			return converter;
		}

		/**
		 * Get the class that represents the Event.
		 * 
		 * @return The class of the Event associated with this event value
		 */
		public Class<E> getEventClass() {
			return event;
		}

		/**
		 * Get the class that represents Value.
		 * 
		 * @return The class of the Value associated with this event value
		 */
		public Class<T> getValueClass() {
			return c;
		}

		/**
		 * Get the classes that represent the excluded for this Event value.
		 * 
		 * @return The classes of the Excludes associated with this event value
		 */
		@Nullable
		@SuppressWarnings("unchecked")
		public Class<? extends Event>[] getExcludes() {
			if (excludes != null)
				return Arrays.copyOf(excludes, excludes.length);
			return new Class[0];
		}

		/**
		 * Get the error message used when encountering an exclude value.
		 * @return The error message to use when encountering an exclude
		 */
		@Nullable
		public String getExcludeErrorMessage() {
			return excludeErrorMessage;
		}

	}

	private final static List<EventValueInfo<?, ?>> defaultEventValues = new ArrayList<>();
	private final static List<EventValueInfo<?, ?>> futureEventValues = new ArrayList<>();
	private final static List<EventValueInfo<?, ?>> pastEventValues = new ArrayList<>();

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
	 * Get Event Values list for the specified time.
	 * 
	 * @param time The time of the event values. One of
	 * {@link EventValues#TIME_PAST}, {@link EventValues#TIME_NOW} or {@link EventValues#TIME_FUTURE}.
	 * @return An immutable copy of the event values list for the specified time.
	 */
	public static List<EventValueInfo<?, ?>> getEventValuesListForTime(int time) {
		return ImmutableList.copyOf(getEventValuesList(time));
	}

	private static List<EventValueInfo<?, ?>> getEventValuesList(int time) {
		if (time == -1)
			return pastEventValues;
		if (time == 0)
			return defaultEventValues;
		if (time == 1)
			return futureEventValues;
		throw new IllegalArgumentException("time must be -1, 0, or 1");
	}

	/**
	 * Registers an event value.
	 * 
	 * @param event the event.
	 * @param c the return type of the event value.
	 * @param getter the getter to get the value.
	 * @param time -1 if this is the value before the event, 1 if after, and 0 if it's the default or this value doesn't have distinct states.
	 *            <b>Always register a default state!</b> You can leave out one of the other states instead, e.g. only register a default and a past state. The future state will
	 *            default to the default state in this case.
	 * @deprecated usage of Getter has been marked for removal in favour of Converter. {@link EventValues#registerEventValue(Class, Class, Converter, int, String, Class...)}
	 */
	@Deprecated
	public static <T, E extends Event> void registerEventValue(Class<E> event, Class<T> c, Getter<T, E> getter, int time) {
		registerEventValue(event, c, (Converter<E, T>) getter, time);
	}

	/**
	 * Registers an event value.
	 * 
	 * @param event the event.
	 * @param c the return type of the event value.
	 * @param converter the converter to get the value.
	 * @param time -1 if this is the value before the event, 1 if after, and 0 if it's the default or this value doesn't have distinct states.
	 *            <b>Always register a default state!</b> You can leave out one of the other states instead, e.g. only register a default and a past state. The future state will
	 *            default to the default state in this case.
	 */
	public static <T, E extends Event> void registerEventValue(Class<E> event, Class<T> c, Converter<E, T> converter, int time) {
		registerEventValue(event, c, converter, time, null, (Class<E>[]) null);
	}

	/**
	 * Same as {@link #registerEventValue(Class, Class, Getter, int)}
	 * 
	 * @param event the event.
	 * @param c the return type of the event value.
	 * @param converter the converter to get the value.
	 * @param time -1 if this is the value before the event, 1 if after, and 0 if it's the default or this value doesn't have distinct states.
	 *            <b>Always register a default state!</b> You can leave out one of the other states instead, e.g. only register a default and a past state. The future state will
	 *            default to the default state in this case.
	 * @param excludes Subclasses of the event for which this event value should not be registered for.
	 */
	@SafeVarargs
	public static <T, E extends Event> void registerEventValue(Class<E> event, Class<T> c, Converter<E, T> converter, int time, @Nullable String excludeErrorMessage, @Nullable Class<? extends Event>... excludes) {
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
	 * @param event the event.
	 * @param c return type of converter.
	 * @param time -1 if this is the value before the event, 1 if after, and 0 if it's the default or this value doesn't have distinct states.
	 *            <b>Always register a default state!</b> You can leave out one of the other states instead, e.g. only register a default and a past state. The future state will
	 *            default to the default state in this case.
	 * @return The event's value.
	 * {@link #registerEventValue(Class, Class, Getter, int)}
	 */
	@Nullable
	public static <T, E extends Event> T getEventValue(E event, Class<T> c, int time) {
		@SuppressWarnings("unchecked")
		Converter<E, T> converter = getEventValueConverter((Class<E>) event.getClass(), c, time);
		if (converter == null)
			return null;
		return converter.convert(event);
	}

	/**
	 * Returns a getter to get a value from in an event.
	 * <p>
	 * Can print an error if the event value is blocked for the given event.
	 * 
	 * @param event the event class the getter will be getting from.
	 * @param c type of getter.
	 * @param time the event-value's time.
	 * @return A getter to get values for a given type of events.
	 * {@link #registerEventValue(Class, Class, Getter, int)}
	 * {@link EventValueExpression#EventValueExpression(Class)}
	 * @deprecated Due for removal, use {@link EventValues#getEventValueConverter(Class, Class, int)}
	 */
	@Deprecated
	@Nullable
	// Note: Skript doesn't use this method anywhere. Purely external addons deprecation.
	public static <T, E extends Event> Getter<? extends T, ? super E> getEventValueGetter(Class<E> event, Class<T> c, int time) {
		Converter<E, T> converter = getEventValueConverter(event, c, time, true);
		return new Getter<T, E>() {
			@Override
			public @Nullable T get(E event) {
				return converter.convert(event);
			}
		};
	}

	/**
	 * Returns a converter to get a value from in an event.
	 * <p>
	 * Can print an error if the event value is blocked for the given event.
	 * 
	 * @param event the event class the getter will be getting from.
	 * @param c type of converter.
	 * @param time the event-value's time.
	 * @return A getter to get values for a given type of events.
	 * {@link #registerEventValue(Class, Class, Getter, int)}
	 * {@link EventValueExpression#EventValueExpression(Class)}
	 */
	@Nullable
	public static <T, E extends Event> Converter<E, T> getEventValueConverter(Class<E> event, Class<T> c, int time) {
		return getEventValueConverter(event, c, time, true);
	}

	@SuppressWarnings("unchecked")
	@Nullable
	private static <T, E extends Event> Converter<E, T> getEventValueConverter(Class<E> event, Class<T> c, int time, boolean allowDefault) {
		List<EventValueInfo<?, ?>> eventValues = getEventValuesList(time);

		// First check for exact classes matching the parameters.
		for (EventValueInfo<?, ?> info : eventValues) {
			if (!c.equals(info.getValueClass()))
				continue;
			if (!checkExcludes(info, event))
				return null;
			if (info.getEventClass().isAssignableFrom(event))
				return (Converter<E, T>) info.getConverter();
		}

		// Second check for assignable subclasses.
		for (EventValueInfo<?, ?> info : eventValues) {
			if (!c.isAssignableFrom(info.getValueClass()))
				continue;
			if (!checkExcludes(info, event))
				return null;
			if (info.getEventClass().isAssignableFrom(event))
				return (Getter<T, E>) info.getConverter();
			if (!event.isAssignableFrom(info.getEventClass()))
				continue;
			return new Converter<E, T>() {
				@Override
				@Nullable
				public T convert(E event) {
					if (!info.getEventClass().isInstance(event))
						return null;
					return ((Converter<E, T>) info.getConverter()).convert(event);
				}
			};
		}

		// Most checks have returned before this below is called, but Skript will attempt to convert or find an alternative.
		// Third check is if the returned object matches the class.
		for (EventValueInfo<?, ?> info : eventValues) {
			if (!info.getValueClass().isAssignableFrom(c))
				continue;
			boolean checkInstanceOf = !info.getEventClass().isAssignableFrom(event);
			if (checkInstanceOf && !event.isAssignableFrom(info.getEventClass()))
				continue;
			if (!checkExcludes(info, event))
				return null;
			return new Getter<T, E>() {
				@Override
				@Nullable
				public T get(E event) {
					if (checkInstanceOf && !info.getEventClass().isInstance(event))
						return null;
					Object object = ((Converter<E, T>) info.getConverter()).convert(event);
					if (c.isInstance(object))
						return (T) object;
					return null;
				}
			};
		}

		// Fourth check will attempt to convert the event value to the type.
		for (EventValueInfo<?, ?> info : eventValues) {
			boolean checkInstanceOf = !info.getEventClass().isAssignableFrom(event);
			if (checkInstanceOf && !event.isAssignableFrom(info.getEventClass()))
				continue;
			Converter<E, T> converter = (Converter<E, T>) getConvertedConverter(info, c, checkInstanceOf);
			if (converter == null)
				continue;
			if (!checkExcludes(info, event))
				return null;
			return converter;
		}

		// If the check should try again matching event values with a 0 time (most event values).
		if (allowDefault && time != 0)
			return getEventValueConverter(event, c, 0, false);
		return null;
	}

	private static <E extends Event> boolean checkExcludes(EventValueInfo<?, ?> info, Class<E> event) {
		if (info.getExcludes().length == 0)
			return true;
		for (Class<? extends Event> exclude : info.getExcludes()) {
			if (exclude.isAssignableFrom(event)) {
				Skript.error(info.excludeErrorMessage);
				return false;
			}
		}
		return true;
	}

	@Nullable
	private static <E extends Event, F, T> Converter<E, T> getConvertedConverter(EventValueInfo<E, F> info, Class<T> to, boolean checkInstanceOf) {
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

	public static boolean doesEventValueHaveTimeStates(Class<? extends Event> event, Class<?> c) {
		return getEventValueConverter(event, c, -1, false) != null || getEventValueConverter(event, c, 1, false) != null;
	}

}
