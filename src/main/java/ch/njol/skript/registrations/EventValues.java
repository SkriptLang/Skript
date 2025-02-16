package ch.njol.skript.registrations;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.util.Getter;
import ch.njol.util.Kleenean;
import com.google.common.collect.ImmutableList;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.Converter;
import org.skriptlang.skript.lang.converter.Converters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
		Class<E> eventClass, Class<T> valueClass,
		Converter<E, T> converter
	) {
		registerEventValue(eventClass, valueClass, converter, TIME_NOW);
	}

	/**
	 * Registers an event value.
	 *
	 * @param eventClass the event valueClass class.
	 * @param valueClass the return valueClass of the converter for the event value.
	 * @param converter the converter to get the value with the provided eventClass.
	 * @param time value of TIME_PAST if this is the value before the eventClass, TIME_FUTURE if after, and TIME_NOW if it's the default or this value doesn't have distinct states.
	 *            <b>Always register a default state!</b> You can leave out one of the other states instead, e.g. only register a default and a past state. The future state will
	 *            default to the default state in this case.
	 */
	public static <T, E extends Event> void registerEventValue(
		Class<E> eventClass, Class<T> valueClass,
		Converter<E, T> converter, int time
	) {
		registerEventValue(eventClass, valueClass, converter, time, null, (Class<? extends E>[]) null);
	}

	/**
	 * Registers an event value and with excluded events.
	 * Excluded events are events that this event value can't operate in.
	 *
	 * @param eventClass the eventClass type class.
	 * @param valueClass the return type of the converter for the event value.
	 * @param converter the converter to get the value with the provided eventClass.
	 * @param time value of TIME_PAST if this is the value before the eventClass, TIME_FUTURE if after, and TIME_NOW if it's the default or this value doesn't have distinct states.
	 *            <b>Always register a default state!</b> You can leave out one of the other states instead, e.g. only register a default and a past state. The future state will
	 *            default to the default state in this case.
	 * @param excludeErrorMessage The error message to display when used in the excluded events.
	 * @param excludes subclasses of the eventClass for which this event value should not be registered for
	 */
	@SafeVarargs
	public static <T, E extends Event> void registerEventValue(
		Class<E> eventClass, Class<T> valueClass,
		Converter<E, T> converter, int time,
		@Nullable String excludeErrorMessage,
		@Nullable Class<? extends E>... excludes
	) {
		Skript.checkAcceptRegistrations();
		List<EventValueInfo<?, ?>> eventValues = getEventValuesList(time);
		EventValueInfo<E, T> element = new EventValueInfo<>(eventClass, valueClass, converter, excludeErrorMessage, excludes);

		for (int i = 0; i < eventValues.size(); i++) {
			EventValueInfo<?, ?> info = eventValues.get(i);
			// We don't care for exact duplicates. Prefer Skript's over any addon.
			if (info.eventClass.equals(eventClass) && info.valueClass.equals(valueClass))
				return;
			// If the events don't match, we prefer the highest subclass eventClass.
			// If the events match, we prefer the highest subclass type.
			if (!info.eventClass.equals(eventClass) ? info.eventClass.isAssignableFrom(eventClass) : info.valueClass.isAssignableFrom(valueClass)) {
				eventValues.add(i, element);
				return;
			}
		}
		eventValues.add(element);
	}

	/**
	 * @deprecated Use {@link #registerEventValue(Class, Class, Converter, int, String, Class[])} instead.
	 */
	@Deprecated(forRemoval = true)
	@SafeVarargs
	@SuppressWarnings({"removal"})
	public static <T, E extends Event> void registerEventValue(
		Class<E> eventClass, Class<T> valueClass,
		Getter<T, E> getter, int time,
		@Nullable String excludeErrorMessage,
		@Nullable Class<? extends E>... excludes
	) {
		registerEventValue(eventClass, valueClass, (Converter<E, T>) getter, time, excludeErrorMessage, excludes);
	}

	/**
	 * @deprecated Use {@link #registerEventValue(Class, Class, Converter, int)} instead.
	 */
	@Deprecated(forRemoval = true)
	@SuppressWarnings({"removal"})
	public static <T, E extends Event> void registerEventValue(
		Class<E> eventClass, Class<T> valueClass,
		Getter<T, E> getter, int time
	) {
		registerEventValue(eventClass, valueClass, (Converter<E, T>) getter, time);
	}

	/**
	 * Gets a specific value from an eventClass. Returns null if the eventClass doesn't have such a value (conversions are done to try and get the desired value).
	 * <p>
	 * It is recommended to use {@link EventValues#getEventValueGetter(Class, Class, int)} or {@link EventValueExpression#EventValueExpression(Class)} instead of invoking this
	 * method repeatedly.
	 *
	 * @param event eventClass
	 * @param valueClass return type of getter
	 * @param time -1 if this is the value before the eventClass, 1 if after, and 0 if it's the default or this value doesn't have distinct states.
	 *            <b>Always register a default state!</b> You can leave out one of the other states instead, e.g. only register a default and a past state. The future state will
	 *            default to the default state in this case.
	 * @return The eventClass's value
	 * @see #registerEventValue(Class, Class, Converter, int)
	 */
	public static <T, E extends Event> @Nullable T getEventValue(E event, Class<T> valueClass, int time) {
		//noinspection unchecked
		Converter<? super E, ? extends T> converter = getEventValueConverter((Class<E>) event.getClass(), valueClass, time);
		if (converter == null)
			return null;
		return converter.convert(event);
	}

	/**
	 * @deprecated Use {@link #getExactEventValueConverter(Class, Class, int)} instead.
	 */
	@Nullable
	@Deprecated(forRemoval = true)
	@SuppressWarnings({"removal"})
	public static <T, E extends Event> Getter<? extends T, ? super E> getExactEventValueGetter(Class<E> eventClass, Class<T> valueClass, int time) {
		return toGetter(getExactEventValueConverter(eventClass, valueClass, time));
	}

	/**
	 * Checks that a {@link Converter} exists for the exact type. No converting or subclass checking.
	 *
	 * @param eventClass the eventClass class the getter will be getting from
	 * @param valueClass type of {@link Converter}
	 * @param time the event-value's time
	 * @return A getter to get values for a given type of events
	 * @see #registerEventValue(Class, Class, Converter, int)
	 * @see EventValueExpression#EventValueExpression(Class)
	 */
	@Nullable
	public static <E extends Event, T> Converter<? super E, ? extends T> getExactEventValueConverter(
		Class<E> eventClass, Class<T> valueClass, int time
	) {
		List<EventValueInfo<?, ?>> eventValues = getEventValuesList(time);
		// First check for exact classes matching the parameters.
		for (EventValueInfo<?, ?> eventValueInfo : eventValues) {
			if (!valueClass.equals(eventValueInfo.valueClass))
				continue;
			if (!checkExcludes(eventValueInfo, eventClass))
				return null;
			if (eventValueInfo.eventClass.isAssignableFrom(eventClass))
				//noinspection unchecked
				return (Converter<? super E, ? extends T>) eventValueInfo.converter;
		}
		return null;
	}

	/**
	 * @deprecated Use {@link #hasMultipleConverters(Class, Class, int)} instead.
	 */
	@Deprecated(forRemoval = true)
	public static <T, E extends Event> Kleenean hasMultipleGetters(Class<E> eventClass, Class<T> valueClass, int time) {
		return hasMultipleConverters(eventClass, valueClass, time);
	}

	/**
	 * Checks if an eventClass has multiple {@link Converter}s, including default ones.
	 *
	 * @param eventClass the eventClass class the {@link Converter} will be getting from.
	 * @param valueClass type of {@link Converter}.
	 * @param time the event-value's time.
	 * @return true or false if the eventClass and type have multiple {@link Converter}s.
	 */
	public static <T, E extends Event> Kleenean hasMultipleConverters(Class<E> eventClass, Class<T> valueClass, int time) {
		List<Converter<? super E, ? extends T>> getters = getEventValueConverters(eventClass, valueClass, time, true, false);
		if (getters == null)
			return Kleenean.UNKNOWN;
		return Kleenean.get(getters.size() > 1);
	}

	/**
	 * @deprecated Use {@link #getEventValueConverter(Class, Class, int)} instead.
	 */
	@Nullable
	@Deprecated(forRemoval = true)
	@SuppressWarnings({"removal"})
	public static <T, E extends Event> Getter<? extends T, ? super E> getEventValueGetter(Class<E> eventClass, Class<T> valueClass, int time) {
		return toGetter(getEventValueConverter(eventClass, valueClass, time, true));
	}

	/**
	 * Returns a {@link Converter} to get a value from in an eventClass.
	 * <p>
	 * Can print an error if the event value is blocked for the given eventClass.
	 *
	 * @param eventClass the eventClass class the {@link Converter} will be getting from.
	 * @param valueClass type of {@link Converter}.
	 * @param time the event-value's time.
	 * @return A getter to get values for a given type of events.
	 * @see #registerEventValue(Class, Class, Converter, int)
	 * @see EventValueExpression#EventValueExpression(Class)
	 */
	public static <T, E extends Event> @Nullable Converter<? super E, ? extends T> getEventValueConverter(
		Class<E> eventClass, Class<T> valueClass, int time
	) {
		return getEventValueConverter(eventClass, valueClass, time, true);
	}

	@Nullable
	private static <T, E extends Event> Converter<? super E, ? extends T> getEventValueConverter(
		Class<E> eventClass, Class<T> valueClass, int time, boolean allowDefault
	) {
		List<Converter<? super E, ? extends T>> list = getEventValueConverters(eventClass, valueClass, time, allowDefault);
		if (list == null || list.isEmpty())
			return null;
		return list.get(0);
	}

	@Nullable
	private static <T, E extends Event> List<Converter<? super E, ? extends T>> getEventValueConverters(
		Class<E> eventClass, Class<T> valueClass, int time, boolean allowDefault
	) {
		return getEventValueConverters(eventClass, valueClass, time, allowDefault, true);
	}

	/*
	 * We need to be able to collect all possible event-values to a list for determining problematic collisions.
	 * Always return after the loop check if the list is not empty.
	 */
	@Nullable
	@SuppressWarnings("unchecked")
	private static <T, E extends Event> List<Converter<? super E, ? extends T>> getEventValueConverters(
		Class<E> eventClass, Class<T> valueClass, int time,
		boolean allowDefault, boolean allowConverting
	) {
		List<EventValueInfo<?, ?>> eventValues = getEventValuesList(time);
		List<Converter<? super E, ? extends T>> list = new ArrayList<>();
		// First check for exact classes matching the parameters.
		Converter<? super E, ? extends T> exact = getExactEventValueConverter(eventClass, valueClass, time);
		if (exact != null) {
			list.add(exact);
			return list;
		}
		Map<EventValueInfo<?, ?>, Converter<? super E, ? extends T>> infoConverterMap = new HashMap<>();
		// Second check for assignable subclasses.
		for (EventValueInfo<?, ?> eventValueInfo : eventValues) {
			if (!valueClass.isAssignableFrom(eventValueInfo.valueClass))
				continue;
			if (!checkExcludes(eventValueInfo, eventClass))
				return null;
			if (eventValueInfo.eventClass.isAssignableFrom(eventClass)) {
				list.add((Converter<? super E, ? extends T>) eventValueInfo.converter);
				infoConverterMap.put(eventValueInfo, (Converter<? super E, ? extends T>) eventValueInfo.converter);
				continue;
			}
			if (!eventClass.isAssignableFrom(eventValueInfo.eventClass))
				continue;
			Converter<? super E, ? extends T> converter = e -> {
				if (!eventValueInfo.eventClass.isInstance(e))
					return null;
				return ((Converter<? super E, ? extends T>) eventValueInfo.converter).convert(e);
			};
			list.add(converter);
			infoConverterMap.put(eventValueInfo, converter);
		}
		if (!list.isEmpty())
			return delegateConverters(eventClass, valueClass, infoConverterMap, list);
		if (!allowConverting)
			return null;
		// Most checks have returned before this below is called, but Skript will attempt to convert or find an alternative.
		// Third check is if the returned object matches the class.
		for (EventValueInfo<?, ?> eventValueInfo : eventValues) {
			if (!eventValueInfo.valueClass.isAssignableFrom(valueClass))
				continue;
			boolean checkInstanceOf = !eventValueInfo.eventClass.isAssignableFrom(eventClass);
			if (checkInstanceOf && !eventClass.isAssignableFrom(eventValueInfo.eventClass))
				continue;

			if (!checkExcludes(eventValueInfo, eventClass))
				return null;

			Converter<? super E, ? extends T> converter = e -> {
				if (checkInstanceOf && !eventValueInfo.eventClass.isInstance(e))
					return null;
				T object = ((Converter<? super E, ? extends T>) eventValueInfo.converter).convert(e);
				if (valueClass.isInstance(object))
					return object;
				return null;
			};
			list.add(converter);
			infoConverterMap.put(eventValueInfo, converter);
		}
		if (!list.isEmpty())
			return delegateConverters(eventClass, valueClass, infoConverterMap, list);
		// Fourth check will attempt to convert the event value to the requesting type.
		// This first for loop will check that the events are exact. See issue #5016
		for (EventValueInfo<?, ?> eventValueInfo : eventValues) {
			if (!eventClass.equals(eventValueInfo.eventClass))
				continue;

			Converter<? super E, ? extends T> converter = (Converter<? super E, ? extends T>)
				getConvertedConverter(eventValueInfo, valueClass, false);
			if (converter == null)
				continue;

			if (!checkExcludes(eventValueInfo, eventClass))
				return null;
			list.add(converter);
			continue;
		}
		if (!list.isEmpty())
			return list;
		// This loop will attempt to look for converters assignable to the class of the provided eventClass.
		for (EventValueInfo<?, ?> eventValueInfo : eventValues) {
			// The requesting eventClass must be assignable to the event value's eventClass. Otherwise it'll throw an error.
			if (!eventClass.isAssignableFrom(eventValueInfo.eventClass))
				continue;

			Converter<? super E, ? extends T> converter = (Converter<? super E, ? extends T>)
				getConvertedConverter(eventValueInfo, valueClass, true);
			if (converter == null)
				continue;

			if (!checkExcludes(eventValueInfo, eventClass))
				return null;
			list.add(converter);
			continue;
		}
		if (!list.isEmpty())
			return list;
		// If the check should try again matching event values with a 0 time (most event values).
		if (allowDefault && time != 0)
			return getEventValueConverters(eventClass, valueClass, 0, false);
		return null;
	}

	/*
		In this method we can delegate/strip converters that are able to be obtainable through their own 'event-classinfo'.
		For example, PlayerTradeEvent has a player value (player who traded) and an AbstractVillager value (villager traded from).
		Beforehand, since there is no Entity value, it was then grabbing both values as they both can be casted as an Entity
			resulting in a parse error of "multiple entities"
		Now, we filter out the ones that can be obtained using their own classinfo, such as 'event-player'
			which leaves us only the AbstractVillager for 'event-entity'
	 */
	private static <E extends Event, T> List<Converter<? super E, ? extends T>> delegateConverters(
		Class<E> eventClass,
		Class<T> valueClass,
		Map<EventValueInfo<?, ?>, Converter<? super E, ? extends T>> infoConverterMap,
		List<Converter<? super E, ? extends T>> converters
	) {
		if (converters.size() == 1)
			return converters;
		ClassInfo<T> valueClassInfo = Classes.getExactClassInfo(valueClass);
		List<Converter<? super E, ? extends T>> delegated = new ArrayList<>();
		for (EventValueInfo<?, ?> eventValueInfo : infoConverterMap.keySet()) {
			ClassInfo<?> thisClassInfo = Classes.getExactClassInfo(eventValueInfo.valueClass);
			if (thisClassInfo != null && !thisClassInfo.equals(valueClassInfo))
				continue;
			delegated.add(infoConverterMap.get(eventValueInfo));
		}
		if (delegated.isEmpty())
			return converters;
		return delegated;
	}

	/**
	 * Check if the event value states to exclude events.
	 * False if the current EventValueInfo cannot operate in the provided eventClass.
	 *
	 * @param info The event value info that will be used to grab the value from
	 * @param eventClass The eventClass class to check the excludes against.
	 * @return boolean if true the event value passes for the events.
	 */
	private static boolean checkExcludes(EventValueInfo<?, ?> info, Class<? extends Event> eventClass) {
		if (info.excludes == null)
			return true;
		for (Class<? extends Event> ex : (Class<? extends Event>[]) info.excludes) {
			if (ex.isAssignableFrom(eventClass)) {
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
	 * @param valueClass The class that the converter will look for to convert the type from the event value to
	 * @param checkInstanceOf If the eventClass must be an exact instance of the event value info's eventClass or not.
	 * @return The found Converter wrapped in a Getter object, or null if no Converter was found.
	 */
	@Nullable
	private static <E extends Event, F, T> Converter<? super E, ? extends T> getConvertedConverter(
		EventValueInfo<E, F> info, Class<T> valueClass, boolean checkInstanceOf
	) {
		Converter<? super F, ? extends T> converter = Converters.getConverter(info.valueClass, valueClass);

		if (converter == null)
			return null;

		return event -> {
			if (checkInstanceOf && !info.eventClass.isInstance(event))
				return null;
			F f = info.converter.convert(event);
			if (f == null)
				return null;
			return converter.convert(f);
		};
	}

	@Deprecated(forRemoval = true)
	@SuppressWarnings({"removal"})
	private static <A, B> Getter<B, A> toGetter(Converter<A, B> converter) {
		if (converter == null)
			return null;

		return new Getter<>() {
			@Override
			public @Nullable B get(A arg) {
				return converter.convert(arg);
			}
		};
	}

	public static boolean doesExactEventValueHaveTimeStates(Class<? extends Event> eventClass, Class<?> valueClass) {
		return getExactEventValueConverter(eventClass, valueClass, TIME_PAST) != null
			|| getExactEventValueConverter(eventClass, valueClass, TIME_FUTURE) != null;
	}

	public static boolean doesEventValueHaveTimeStates(Class<? extends Event> eventClass, Class<?> valueClass) {
		return getEventValueConverter(eventClass, valueClass, TIME_PAST, false) != null
			|| getEventValueConverter(eventClass, valueClass, TIME_FUTURE, false) != null;
	}

	private record EventValueInfo<E extends Event, T>(
		Class<E> eventClass, Class<T> valueClass, Converter<E, T> converter,
		@Nullable String excludeErrorMessage,
		@Nullable Class<? extends E>[] excludes
	) {
		private EventValueInfo {
			assert eventClass != null;
			assert valueClass != null;
			assert converter != null;
		}

	}

}
