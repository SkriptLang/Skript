package ch.njol.skript.registrations;

import ch.njol.skript.Skript;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.util.Kleenean;
import com.google.common.collect.ImmutableList;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.Converter;
import org.skriptlang.skript.lang.converter.Converters;

import java.util.ArrayList;
import java.util.List;

public class EventValues {

	private EventValues() {}

	/**
	 * The past value of an event value. Represented by "past" or "former".
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

	private final static List<EventValueInfo<?, ?>> defaultEventValues = new ArrayList<>(30);
	private final static List<EventValueInfo<?, ?>> futureEventValues = new ArrayList<>();
	private final static List<EventValueInfo<?, ?>> pastEventValues = new ArrayList<>();

	/**
	 * Get Event Values list for the specified time
	 * @param time The time of the event values. One of
	 * {@link EventValues#TIME_PAST}, {@link EventValues#TIME_NOW} or {@link EventValues#TIME_FUTURE}.
	 * @return An immutable copy of the event values list for the specified time
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
	 * Registers an event value, specified by the provided {@link Converter}, with excluded events.
	 * Uses the default time, {@link #TIME_NOW}.
	 *
	 * @see #registerEventValue(Class, Class, Converter, int)
	 */
	public static <T, E extends Event> void registerEventValue(
		Class<E> event, Class<T> type,
		Converter<E, T> converter
	) {
		registerEventValue(event, type, converter, TIME_NOW);
	}

	/**
	 * Registers an event value.
	 *
	 * @param event the event type class.
	 * @param type the return type of the converter for the event value.
	 * @param converter the converter to get the value with the provided event.
	 * @param time value of TIME_PAST if this is the value before the event, TIME_FUTURE if after, and TIME_NOW if it's the default or this value doesn't have distinct states.
	 *            <b>Always register a default state!</b> You can leave out one of the other states instead, e.g. only register a default and a past state. The future state will
	 *            default to the default state in this case.
	 */
	public static <T, E extends Event> void registerEventValue(
		Class<E> event, Class<T> type,
		Converter<E, T> converter, int time
	) {
		registerEventValue(event, type, converter, time, null, (Class<? extends E>[]) null);
	}

	/**
	 * Registers an event value and with excluded events.
	 * Excluded events are events that this event value can't operate in.
	 *
	 * @param event the event type class.
	 * @param type the return type of the converter for the event value.
	 * @param converter the converter to get the value with the provided event.
	 * @param time value of TIME_PAST if this is the value before the event, TIME_FUTURE if after, and TIME_NOW if it's the default or this value doesn't have distinct states.
	 *            <b>Always register a default state!</b> You can leave out one of the other states instead, e.g. only register a default and a past state. The future state will
	 *            default to the default state in this case.
	 * @param excludeErrorMessage The error message to display when used in the excluded events.
	 * @param excludes subclasses of the event for which this event value should not be registered for
	 */
	@SafeVarargs
	public static <T, E extends Event> void registerEventValue(
		Class<E> event, Class<T> type,
		Converter<E, T> converter, int time,
		@Nullable String excludeErrorMessage,
		@Nullable Class<? extends E>... excludes
	) {
		Skript.checkAcceptRegistrations();
		List<EventValueInfo<?, ?>> eventValues = getEventValuesList(time);
		EventValueInfo<E, T> element = new EventValueInfo<>(event, type, converter, excludeErrorMessage, excludes);

		for (int i = 0; i < eventValues.size(); i++) {
			EventValueInfo<?, ?> info = eventValues.get(i);
			// We don't care for exact duplicates. Prefer Skript's over any addon.
			if (info.event.equals(event) && info.c.equals(type))
				return;
			// If the events don't match, we prefer the highest subclass event.
			// If the events match, we prefer the highest subclass type.
			if (!info.event.equals(event) ? info.event.isAssignableFrom(event) : info.c.isAssignableFrom(type)) {
				eventValues.add(i, element);
				return;
			}
		}
		eventValues.add(element);
	}

	/**
	 * Gets a specific value from an event. Returns null if the event doesn't have such a value (conversions are done to try and get the desired value).
	 * <p>
	 * It is recommended to use {@link EventValues#getEventValueGetter(Class, Class, int)} or {@link EventValueExpression#EventValueExpression(Class)} instead of invoking this
	 * method repeatedly.
	 *
	 * @param e event
	 * @param c return type of getter
	 * @param time -1 if this is the value before the event, 1 if after, and 0 if it's the default or this value doesn't have distinct states.
	 *            <b>Always register a default state!</b> You can leave out one of the other states instead, e.g. only register a default and a past state. The future state will
	 *            default to the default state in this case.
	 * @return The event's value
	 * @see #registerEventValue(Class, Class, Converter, int)
	 */
	@Nullable
	public static <T, E extends Event> T getEventValue(E e, Class<T> c, int time) {
		@SuppressWarnings("unchecked")
		Converter<? super E, ? extends T> getter = getEventValueGetter((Class<E>) e.getClass(), c, time);
		if (getter == null)
			return null;
		return getter.convert(e);
	}

	/**
	 * Checks that a getter exists for the exact type. No converting or subclass checking.
	 *
	 * @param event the event class the getter will be getting from
	 * @param c type of getter
	 * @param time the event-value's time
	 * @return A getter to get values for a given type of events
	 * @see #registerEventValue(Class, Class, Converter, int)
	 * @see EventValueExpression#EventValueExpression(Class)
	 */
	@Nullable
	@SuppressWarnings("unchecked")
	public static <T, E extends Event> Converter<? super E, ? extends T> getExactEventValueGetter(Class<E> event, Class<T> c, int time) {
		List<EventValueInfo<?, ?>> eventValues = getEventValuesList(time);
		// First check for exact classes matching the parameters.
		for (EventValueInfo<?, ?> eventValueInfo : eventValues) {
			if (!c.equals(eventValueInfo.c))
				continue;
			if (!checkExcludes(eventValueInfo, event))
				return null;
			if (eventValueInfo.event.isAssignableFrom(event))
				return (Converter<? super E, ? extends T>) eventValueInfo.converter;
		}
		return null;
	}

	/**
	 * Checks if an event has multiple getters, including default ones.
	 *
	 * @param event the event class the getter will be getting from.
	 * @param type type of getter.
	 * @param time the event-value's time.
	 * @return true or false if the event and type have multiple getters.
	 */
	public static <T, E extends Event> Kleenean hasMultipleGetters(Class<E> event, Class<T> type, int time) {
		List<Converter<? super E, ? extends T>> getters = getEventValueGetters(event, type, time, true, false);
		if (getters == null)
			return Kleenean.UNKNOWN;
		return Kleenean.get(getters.size() > 1);
	}

	/**
	 * Returns a getter to get a value from in an event.
	 * <p>
	 * Can print an error if the event value is blocked for the given event.
	 *
	 * @param event the event class the getter will be getting from.
	 * @param type type of getter.
	 * @param time the event-value's time.
	 * @return A getter to get values for a given type of events.
	 * @see #registerEventValue(Class, Class, Converter, int)
	 * @see EventValueExpression#EventValueExpression(Class)
	 */
	@Nullable
	public static <T, E extends Event> Converter<? super E, ? extends T> getEventValueGetter(Class<E> event, Class<T> type, int time) {
		return getEventValueGetter(event, type, time, true);
	}

	@Nullable
	private static <T, E extends Event> Converter<? super E, ? extends T> getEventValueGetter(Class<E> event, Class<T> type, int time, boolean allowDefault) {
		List<Converter<? super E, ? extends T>> list = getEventValueGetters(event, type, time, allowDefault);
		if (list == null || list.isEmpty())
			return null;
		return list.get(0);
	}

	@Nullable
	private static <T, E extends Event> List<Converter<? super E, ? extends T>> getEventValueGetters(Class<E> event, Class<T> type, int time, boolean allowDefault) {
		return getEventValueGetters(event, type, time, allowDefault, true);
	}

	/*
	 * We need to be able to collect all possible event-values to a list for determining problematic collisions.
	 * Always return after the loop check if the list is not empty.
	 */
	@Nullable
	@SuppressWarnings("unchecked")
	private static <T, E extends Event> List<Converter<? super E, ? extends T>> getEventValueGetters(Class<E> event, Class<T> type, int time, boolean allowDefault, boolean allowConverting) {
		List<EventValueInfo<?, ?>> eventValues = getEventValuesList(time);
		List<Converter<? super E, ? extends T>> list = new ArrayList<>();
		// First check for exact classes matching the parameters.
		Converter<? super E, ? extends T> exact = getExactEventValueGetter(event, type, time);
		if (exact != null) {
			list.add(exact);
			return list;
		}
		// Second check for assignable subclasses.
		for (EventValueInfo<?, ?> eventValueInfo : eventValues) {
			if (!type.isAssignableFrom(eventValueInfo.c))
				continue;
			if (!checkExcludes(eventValueInfo, event))
				return null;
			if (eventValueInfo.event.isAssignableFrom(event)) {
				list.add((Converter<? super E, ? extends T>) eventValueInfo.converter);
				continue;
			}
			if (!event.isAssignableFrom(eventValueInfo.event))
				continue;
			list.add(e -> {
				if (!eventValueInfo.event.isInstance(e))
					return null;
				return ((Converter<? super E, ? extends T>) eventValueInfo.converter).convert(e);
			});
		}
		if (!list.isEmpty())
			return list;
		if (!allowConverting)
			return null;
		// Most checks have returned before this below is called, but Skript will attempt to convert or find an alternative.
		// Third check is if the returned object matches the class.
		for (EventValueInfo<?, ?> eventValueInfo : eventValues) {
			if (!eventValueInfo.c.isAssignableFrom(type))
				continue;
			boolean checkInstanceOf = !eventValueInfo.event.isAssignableFrom(event);
			if (checkInstanceOf && !event.isAssignableFrom(eventValueInfo.event))
				continue;

			if (!checkExcludes(eventValueInfo, event))
				return null;

			list.add(e -> {
				if (checkInstanceOf && !eventValueInfo.event.isInstance(e))
					return null;
				T object = ((Converter<? super E, ? extends T>) eventValueInfo.converter).convert(e);
				if (type.isInstance(object))
					return object;
				return null;
			});
		}
		if (!list.isEmpty())
			return list;
		// Fourth check will attempt to convert the event value to the requesting type.
		// This first for loop will check that the events are exact. See issue #5016
		for (EventValueInfo<?, ?> eventValueInfo : eventValues) {
			if (!event.equals(eventValueInfo.event))
				continue;

			Converter<? super E, ? extends T> getter = (Converter<? super E, ? extends T>) getConvertedGetter(eventValueInfo, type, false);
			if (getter == null)
				continue;

			if (!checkExcludes(eventValueInfo, event))
				return null;
			list.add(getter);
			continue;
		}
		if (!list.isEmpty())
			return list;
		// This loop will attempt to look for converters assignable to the class of the provided event.
		for (EventValueInfo<?, ?> eventValueInfo : eventValues) {
			// The requesting event must be assignable to the event value's event. Otherwise it'll throw an error.
			if (!event.isAssignableFrom(eventValueInfo.event))
				continue;

			Converter<? super E, ? extends T> getter = (Converter<? super E, ? extends T>) getConvertedGetter(eventValueInfo, type, true);
			if (getter == null)
				continue;

			if (!checkExcludes(eventValueInfo, event))
				return null;
			list.add(getter);
			continue;
		}
		if (!list.isEmpty())
			return list;
		// If the check should try again matching event values with a 0 time (most event values).
		if (allowDefault && time != 0)
			return getEventValueGetters(event, type, 0, false);
		return null;
	}

	/**
	 * Check if the event value states to exclude events.
	 * False if the current EventValueInfo cannot operate in the provided event.
	 *
	 * @param info The event value info that will be used to grab the value from
	 * @param event The event class to check the excludes against.
	 * @return boolean if true the event value passes for the events.
	 */
	private static boolean checkExcludes(EventValueInfo<?, ?> info, Class<? extends Event> event) {
		if (info.excludes == null)
			return true;
		for (Class<? extends Event> ex : (Class<? extends Event>[]) info.excludes) {
			if (ex.isAssignableFrom(event)) {
				Skript.error(info.excludeErrorMessage);
				return false;
			}
		}
		return true;
	}

	/**
	 * Return a converter wrapped in a getter that will grab the requested value by converting from the given event value info.
	 *
	 * @param info The event value info that will be used to grab the value from
	 * @param to The class that the converter will look for to convert the type from the event value to
	 * @param checkInstanceOf If the event must be an exact instance of the event value info's event or not.
	 * @return The found Converter wrapped in a Getter object, or null if no Converter was found.
	 */
	@Nullable
	private static <E extends Event, F, T> Converter<? super E, ? extends T> getConvertedGetter(EventValueInfo<E, F> info, Class<T> to, boolean checkInstanceOf) {
		Converter<? super F, ? extends T> converter = Converters.getConverter(info.c, to);

		if (converter == null)
			return null;

		return event -> {
			if (checkInstanceOf && !info.event.isInstance(event))
				return null;
			F f = info.converter.convert(event);
			if (f == null)
				return null;
			return converter.convert(f);
		};
	}

	public static boolean doesExactEventValueHaveTimeStates(Class<? extends Event> event, Class<?> c) {
		return getExactEventValueGetter(event, c, TIME_PAST) != null || getExactEventValueGetter(event, c, TIME_FUTURE) != null;
	}

	public static boolean doesEventValueHaveTimeStates(Class<? extends Event> event, Class<?> c) {
		return getEventValueGetter(event, c, TIME_PAST, false) != null || getEventValueGetter(event, c, TIME_FUTURE, false) != null;
	}

	private record EventValueInfo<E extends Event, T>(
		Class<E> event, Class<T> c, Converter<E, T> converter,
		@Nullable String excludeErrorMessage,
		@Nullable Class<? extends E>[] excludes
	) {
		private EventValueInfo {
			assert event != null;
			assert c != null;
			assert converter != null;
		}

	}

}
