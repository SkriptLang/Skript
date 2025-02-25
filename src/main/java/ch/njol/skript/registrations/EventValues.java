package ch.njol.skript.registrations;

import ch.njol.skript.Skript;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.util.Getter;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.Converter;
import org.skriptlang.skript.lang.converter.Converters;

import java.util.*;

public class EventValues<E extends Event, T> {

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

	private static final Map<Class<? extends Event>, EventContext<?, ?>> pastEventValues = new HashMap<>();
	private static final Map<Class<? extends Event>, EventContext<?, ?>> nowEventValues = new HashMap<>();
	private static final Map<Class<? extends Event>, EventContext<?, ?>> futureEventValues = new HashMap<>();

	private static Map<Class<? extends Event>, EventContext<? extends Event, ?>> getEventContextMap(int time) {
		if (time == TIME_PAST) {
			return pastEventValues;
		} else if (time == TIME_NOW) {
			return nowEventValues;
		} else if (time == TIME_FUTURE) {
			return futureEventValues;
		}
		throw new IllegalArgumentException("Invalid time argument.");
	}

	/**
	 * Registers an event value, specified by the provided {@code valueClass} using the {@link Converter}, with excluded events.
	 * Uses the default time, {@link #TIME_NOW}.
	 *
	 * @see #registerEventValue(Class, Class, Converter, int)
	 */
	public static <E extends Event, T> void registerEventValue(
		Class<E> eventClass,
		Class<T> valueClass,
		Converter<E, T> converter
	) {
		registerEventValue(eventClass, valueClass, converter, TIME_NOW);
	}

	/**
	 * Registers an event value.
	 *
	 * @param eventClass The class of the event
	 * @param valueClass The class of the expected event value
	 * @param converter The converter to get the value with the provided event.
	 * @param time The value of TIME_PAST if this is the value before the event, TIME_FUTURE if after, and TIME_NOW if it's the default or this value doesn't have distinct states.
	 *            <b>Always register a default state!</b> You can leave out one of the other states instead, e.g. only register a default and a past state. The future state will
	 *            default to the default state in this case.
	 */
	public static <E extends Event, T> void registerEventValue(
		Class<E> eventClass,
		Class<T> valueClass,
		Converter<E, T> converter,
		int time
	)  {
		registerEventValue(eventClass, valueClass, converter, time, null, (Class<? extends E>[]) null);
	}

	/**
	 * Registers an event value and with excluded events.
	 * Excluded events are events that this event value can't operate in.
	 *
	 * @param eventClass The class of the event
	 * @param valueClass The class of the expected event value
	 * @param converter The converter to get the value with the provided event.
	 * @param time The value of TIME_PAST if this is the value before the event, TIME_FUTURE if after, and TIME_NOW if it's the default or this value doesn't have distinct states.
	 *            <b>Always register a default state!</b> You can leave out one of the other states instead, e.g. only register a default and a past state. The future state will
	 *            default to the default state in this case.
	 * @param excludeMessage The error message to display when used in the excluded events.
	 * @param excludedEvents Subclasses of the event for which this event value should not be registered for
	 */
	@SafeVarargs
	public static <E extends Event, T> void registerEventValue(
		Class<E> eventClass,
		Class<T> valueClass,
		Converter<E, T> converter,
		int time,
		@Nullable String excludeMessage,
		@Nullable Class<? extends E>... excludedEvents
	) {
		Skript.checkAcceptRegistrations();
		EventContext<E, T> eventContext = getEventContext(eventClass, time);
		if (eventContext.getEventValueContext(valueClass) == null) {
			EventValueContext<E, T> valueContext = new EventValueContext<>(eventClass, valueClass, converter, excludeMessage, excludedEvents);
			eventContext.addEventValueContext(valueContext);
		}
	}

	/**
	 * Get the {@link EventContext} correlating to {@code eventClass} of the preset time state.
	 * @param eventClass The class of the event
	 * @return The {@link EventContext} for {@code eventClass}
	 */
	public static <E extends Event, T> @NotNull EventContext<E, T> getEventContext(Class<E> eventClass) {
		return getEventContext(eventClass, 0);
	}

	/**
	 * Get the {@link EventContext} correlating to {@code eventClass} of the provided time state.
	 * {@link #TIME_PAST} , {@link #TIME_NOW} , {@link #TIME_FUTURE}.
	 * @param eventClass The class of the event
	 * @param time The value of TIME_PAST if this is the value before the event, TIME_FUTURE if after, and TIME_NOW if it's the default or this value doesn't have distinct states.
	 *             <b>Always register a default state!</b> You can leave out one of the other states instead, e.g. only register a default and a past state. The future state will
	 *             default to the default state in this case.
	 * @return The {@link EventContext} for {@code eventClass} of the time state from {@code time}
	 */
	public static <E extends Event, T> @NotNull EventContext<E, T> getEventContext(Class<E> eventClass, int time) {
		Map<Class<? extends Event>, EventContext<? extends Event, ?>> eventContextMap = getEventContextMap(time);
		if (eventContextMap == null) {
			throw new IllegalStateException();
		} else if (eventContextMap.containsKey(eventClass)) {
			//noinspection unchecked
			return (EventContext<E, T>) eventContextMap.get(eventClass);
		}
		EventContext<E, T> eventContext = new EventContext<>(eventClass);
		eventContextMap.put(eventClass, eventContext);
		return eventContext;
	}

	/**
	 * Get all template {@link EventContext}s provided by {@code eventClass} in all time states.
	 * @param eventClass The class of the event
	 * @return An {@link Arrays} of all template {@link EventContext}s
	 */
	public static <E extends Event, T> List<EventContext<E, T>> getEventContexts(Class<E> eventClass) {
		List<EventContext<E, T>> eventContexts = new ArrayList<>();
		eventContexts.add(getEventContext(eventClass, -1));
		eventContexts.add(getEventContext(eventClass, 0));
		eventContexts.add(getEventContext(eventClass, 1));
		return eventContexts;
	}

	/**
	 * Get a built {@link EventContext} provided by {@code event} in the present time state.
	 * @param event An event
	 * @return The built {@link EventContext}
	 */
	public static <E extends Event, T> @NotNull EventContext<E, T> getEventContext(E event) {
		return getEventContext(event, 0);
	}

	/**
	 * Get a built {@link EventContext} provided by {@code event} in the specified time state.
	 * @param event An event
	 * @param time The value of TIME_PAST if this is the value before the event, TIME_FUTURE if after, and TIME_NOW if it's the default or this value doesn't have distinct states.
	 *             <b>Always register a default state!</b> You can leave out one of the other states instead, e.g. only register a default and a past state. The future state will
	 *             default to the default state in this case.
	 * @return The built {@link EventContext}
	 */
	public static <E extends Event, T> @NotNull EventContext<E, T> getEventContext(E event, int time) {
		//noinspection unchecked
		EventContext<E, T> eventContext = (EventContext<E, T>) getEventContext(event.getClass(), time);
		return eventContext.buildContext(event);
	}

	/**
	 * Get all {@link EventContext} provided by {@code eventCLass} in all time states
	 * @param event An event
	 * @return An {@link Arrays} of all built {@link EventContext}s
	 */
	public static <E extends Event, T> List<EventContext<E, T>> getEventContexts(E event) {
		List<EventContext<E, T>> builtContexts = new ArrayList<>();
		builtContexts.add(getEventContext(event, TIME_PAST));
		builtContexts.add(getEventContext(event, TIME_NOW));
		builtContexts.add(getEventContext(event, TIME_FUTURE));
		return builtContexts;
	}

	/**
	 * Get an {@link EventValueContext} specified by {@code valueClass} within the {@link EventContext} specified by {@code eventClass}
	 * and in the specified time state {@code time}
	 * @param eventClass The class of the event
	 * @param valueClass The class of the expected event value
	 * @param time The event-value's time.
	 * @return The correlating {@link EventValueContext}
	 */
	public static <E extends Event, T> @Nullable EventValueContext<E, T> getEventValueContext(Class<E> eventClass, Class<T> valueClass, int time) {
		Map<Class<? extends Event>, EventContext<? extends Event, ?>> eventContextMap = getEventContextMap(time);
		if (eventContextMap == null) {
			throw new IllegalStateException();
		} else if (!eventContextMap.containsKey(eventClass)) {
			return null;
		}
		//noinspection unchecked
		EventContext<E, T> eventContext = (EventContext<E, T>) eventContextMap.get(eventClass);
		return eventContext.getEventValueContext(valueClass);
	}

	/**
	 * Checks if the correlating {@link EventContext} from {@code eventClass}
	 * has multiple {@link Converter}s, including default ones, for the {@code valueClass}
	 *
	 * @param eventClass The class of the event
	 * @param valueClass The class of the expected event value
	 * @param time The event-value's time.
	 * @return true or false if the event and type have multiple {@link Converter}s.
	 */
	public static <E extends Event, T> Kleenean hasMultipleConverters(Class<E> eventClass, Class<T> valueClass, int time) {
		List<Converter<? super E, ? extends T>> converters = getEventValueConverters(eventClass, valueClass, time, true, false);
		if (converters == null)
			return Kleenean.UNKNOWN;
		return Kleenean.get(converters.size() > 1);
	}

	/**
	 * Gets a {@link Converter} by checking that the correlating {@link EventValueContext} specified by {@code valueClass}
	 * within the {@link EventContext} specified by {@code eventClass} has an exact singular converter for the event-value.
	 *
	 * @param eventClass The class of the event
	 * @param valueClass The class of the expected event value
	 * @param time The event-value's time
	 * @return A {@link Converter} used to get the event-value
	 * @see #registerEventValue(Class, Class, Converter, int)
	 * @see EventValueExpression#EventValueExpression(Class)
	 */
	public static <E extends Event, T> @Nullable Converter<? super E, ? extends T> getExactEventValueConverter(
		Class<E> eventClass,
		Class<T> valueClass,
		int time
	) {
		EventContext<E, T> thisEventContext = getEventContext(eventClass, time);
		EventValueContext<E, T> thisValueContext = thisEventContext.getEventValueContext(valueClass);
		if (thisValueContext != null) {
			if (!thisValueContext.checkExcluded(eventClass) || !thisValueContext.isSingleConverter())
				return null;
			return thisValueContext.getConverter();
		}
		Map<Class<? extends Event>, EventContext<? extends Event, ?>> eventContextMap = getEventContextMap(time);
		Collection<EventContext<? extends Event, ?>> mapValues = eventContextMap.values();
		for (EventContext<? extends Event, ?> eventContext : mapValues) {
			for (EventValueContext<? extends Event, ?> valueContext : eventContext.getEventValueContexts()) {
				if (!valueClass.equals(valueContext.getValueClass()))
					continue;
				if (!valueContext.checkExcluded(eventClass))
					return null;
				if (eventContext.getEventClass().isAssignableFrom(eventClass) && valueContext.isSingleConverter()) {
					//noinspection unchecked
					Converter<? super E, ? extends T> converter = (Converter<? super E, ? extends T>) valueContext.getConverter();
					cacheEventValues(thisEventContext, valueClass, List.of(converter));
					return converter;
				}
			}
		}
		return null;
	}

	/**
	 * Gets a {@link Converter} from the correlating {@link EventValueContext} specified by {@code valueClass}
	 * within the {@link EventContext} specified by {@code eventClass}.
	 * <p>
	 * Can print an error if the event value is blocked for the given event.
	 *
	 * @param eventClass The class of the event
	 * @param valueClass The class of the expected event value
	 * @param time the event-value's time.
	 * @return A {@link Converter} used to get the event-value
	 * @see #registerEventValue(Class, Class, Converter, int)
	 * @see EventValueExpression#EventValueExpression(Class)
	 */
	public static <E extends Event, T> @Nullable Converter<? super E, ? extends T> getEventValueConverter(
		Class<E> eventClass,
		Class<T> valueClass,
		int time
	) {
		return getEventValueConverter(eventClass, valueClass, time, true);
	}

	private static <E extends Event, T> @Nullable Converter<? super E, ? extends T> getEventValueConverter(
		Class<E> eventClass,
		Class<T> valueClass,
		int time,
		boolean allowDefault
	) {
		List<Converter<? super E, ? extends T>> converters = getEventValueConverters(eventClass, valueClass, time, allowDefault);
		if (converters == null || converters.isEmpty())
			return null;
		return converters.get(0);
	}

	private static @Nullable <E extends Event, T> List<Converter<? super E, ? extends T>> getEventValueConverters(
		Class<E> eventClass,
		Class<T> valueClass,
		int time,
		boolean allowDefault
	) {
		return getEventValueConverters(eventClass, valueClass, time, allowDefault, true);
	}

	private static @Nullable <E extends Event, T> List<Converter<? super E, ? extends T>> getEventValueConverters(
		Class<E> eventClass,
		Class<T> valueClass,
		int time,
		boolean allowDefault,
		boolean allowConvert
	) {
		// 1. Find event-values that exactly match. (targetEvent -> targetType)
		Converter<? super E, ? extends T> exactConverter = getExactEventValueConverter(eventClass, valueClass, time);
		if (exactConverter != null)
			return List.of(exactConverter);
		EventContext<E, T> thisEventContext = getEventContext(eventClass, time);
		EventValueContext<E, T> thisValueContext = thisEventContext.getEventValueContext(valueClass);
		if (thisValueContext != null) {
			return thisValueContext.getConverters();
		}

		Map<Class<? extends Event>, EventContext<? extends Event, ?>> eventContextMap = getEventContextMap(time);
		List<Converter<? super E, ? extends T>> converters = new ArrayList<>();
		Collection<EventContext<? extends Event, ?>> mapValues = eventContextMap.values();


		// 2. Find event-values where the type is a match or subclass of the target, and the event is a
		// superclass or subclass of the target. (superEvent/targetEvent/subEvent -> targetType/subType)
		for (EventContext<? extends Event, ?> eventContext : mapValues) {
			if (eventContext.getEventClass().equals(eventClass))
				continue;
			for (EventValueContext<? extends Event, ?> valueContext : eventContext.getEventValueContexts()) {
				if (!valueClass.isAssignableFrom(valueContext.valueClass))
					continue;
				if (!valueContext.checkExcluded(eventClass))
					return null;
				if (eventContext.getEventClass().isAssignableFrom(eventClass)) {
					//noinspection unchecked
					converters.add((Converter<? super E, ? extends T>) valueContext.getConverter());
					continue;
				}
				if (!eventClass.isAssignableFrom(eventContext.getEventClass()))
					continue;
				converters.add(event -> {
					if (!eventContext.getEventClass().isInstance(event))
						return null;
					//noinspection unchecked
					return ((Converter<E, T>) valueContext.getConverter()).convert(event);
				});
			}
		}
		if (!converters.isEmpty())
			return cacheEventValues(thisEventContext, valueClass, converters);
		if (!allowConvert)
			return null;

		// 3. If no event-values have been found, find event-values where the type is a supertype of the target.
		// (superEvent/targetEvent/subEvent -> superType)
		for (EventContext<? extends Event, ?> eventContext : mapValues) {
			for (EventValueContext<? extends Event, ?> valueContext : eventContext.getEventValueContexts()) {
				if (!valueContext.getValueClass().isAssignableFrom(valueClass))
					continue;
				boolean checkInstanceOf = !eventContext.getEventClass().isAssignableFrom(eventClass);
				if (checkInstanceOf && !eventClass.isAssignableFrom(eventContext.getEventClass()))
					continue;

				if (!valueContext.checkExcluded(eventClass))
					return null;

				converters.add(event -> {
					if (checkInstanceOf && !eventContext.getEventClass().isInstance(event))
						return null;
					//noinspection unchecked
					T object = ((Converter<E, T>) valueContext.getConverter()).convert(event);
					if (valueClass.isInstance(object))
						return object;
					return null;
				});
			}
		}
		if (!converters.isEmpty())
			return cacheEventValues(thisEventContext, valueClass, converters);;

		// 4. If still no event-values exist, and conversions are allowed, find event-values that exactly match the target
		// event AND that return a type we can convert into the right type. (targetEvent -> ? -> targetType)
		for (EventValueContext<? extends Event, ?> valueContext : thisEventContext.getEventValueContexts()) {
			//noinspection unchecked
			Converter<E, T> converter = (Converter<E, T>)
				getConvertedConverter(valueContext, valueClass, false);
			if (converter == null)
				continue;

			if (!valueContext.checkExcluded(eventClass))
				return null;
			converters.add(converter);
		}
		if (!converters.isEmpty())
			return cacheEventValues(thisEventContext, valueClass, converters);;

		// 5. Finally, find event-values that have an event that's a subclass of the target event with the same
		// conversion strategy as 4. (subEvent -> ? -> targetType)
		for (EventContext<? extends Event, ?> eventContext : mapValues) {
			if (!eventClass.isAssignableFrom(eventContext.getEventClass()))
				continue;
			for (EventValueContext<? extends Event, ?> valueContext : eventContext.getEventValueContexts()) {
				//noinspection unchecked
				Converter<? super E, ? extends T> converter = (Converter<? super E, ? extends T>)
					getConvertedConverter(valueContext, valueClass, true);
				if (converter == null)
					continue;

				if (!valueContext.checkExcluded(eventClass))
					return null;
				converters.add(converter);
			}
		}
		if (!converters.isEmpty())
			return cacheEventValues(thisEventContext, valueClass, converters);;

		// 6. Start from 1. with the timestate of present, if allowed and the timestate wasn't already present.
		if (allowDefault && time != TIME_NOW)
			return cacheEventValues(
				thisEventContext,
				valueClass,
				getEventValueConverters(eventClass, valueClass, TIME_NOW, allowDefault, allowConvert)
			);
		return null;
	}

	/**
	 * Return a converter wrapped in a getter that will grab the requested value by converting from the given event value info.
	 *
	 * @param valueContext The event value context that is a superclass of the expected value class
	 * @param valueClass The class that the converter will look for to convert the type from the event value to
	 * @param checkInstanceOf If the event must be an exact instance of the event value info's event or not.
	 * @return The found Converter wrapped in a Getter object, or null if no Converter was found.
	 */
	private static <E extends Event, F, T> Converter<? super E, ? extends T> getConvertedConverter(
		EventValueContext<E, F> valueContext,
		Class<T> valueClass,
		boolean checkInstanceOf
	) {
		Converter<? super F, ? extends T> converter = Converters.getConverter(valueContext.getValueClass(), valueClass);

		if (converter == null)
			return null;

		return event -> {
			if (checkInstanceOf && !valueContext.getEventClass().isInstance(event))
				return null;
			F object = valueContext.getConverter().convert(event);
			if (object == null)
				return null;
			return converter.convert(object);
		};
	}

	/**
	 * Cache event values into {@link EventContext}s so {@link #getEventValueConverters(Class, Class, int, boolean)}
	 * does not have to be reran more than once for the same event class and event value class
	 */
	private static <E extends Event, T> @Nullable List<Converter<? super E, ? extends T>> cacheEventValues(
		EventContext<E, T> eventContext,
		Class<T> valueClass,
		List<Converter<? super E, ? extends T>> converters
	) {
		EventValueContext<E, T> valueContext = new EventValueContext<>(
			eventContext.getEventClass(),
			valueClass,
			converters
		);
		eventContext.addEventValueContext(valueContext);
		return converters;
	}

	public static <E extends Event, T> boolean doesExactEventValueHaveTimeStates(Class<E> eventClass, Class<T> valueClass) {
		return getExactEventValueConverter(eventClass, valueClass, TIME_PAST) != null
			|| getExactEventValueConverter(eventClass, valueClass, TIME_FUTURE) != null;
	}

	public static <E extends Event, T> boolean doesEventValueHaveTimeStates(Class<E> eventClass, Class<T> valueClass) {
		return getEventValueConverter(eventClass, valueClass, TIME_PAST, false) != null
			|| getEventValueConverter(eventClass, valueClass, TIME_FUTURE, false) != null;
	}

	public static class EventContext<E extends Event, T> {
		private E event;
		private final Class<E> eventClass;
		private final List<EventValueContext<E, T>> valueContexts = new ArrayList<>();
		private final Map<Event, EventContext<E, T>> activeContexts = new HashMap<>();

		public EventContext(Class<E> eventClass) {
			this.eventClass = eventClass;
		}

		private EventContext(E event, Class<E> eventClass) {
			this.event = event;
			this.eventClass = eventClass;
		}

		/**
		 * Build this {@link EventContext} by cloning and returning with the {@link EventValueContext}s built
		 * and storing the event values
		 * @param event
		 * @return
		 */
		public EventContext<E, T> buildContext(Event event) {
			assert eventClass.equals(event.getClass());
			if (activeContexts.containsKey(event)) {
				return activeContexts.get(event);
			}
			//noinspection unchecked
			EventContext<E, T> newEventContext = new EventContext<E, T>((E) event, eventClass);
			for (EventValueContext<E, T> valueContext : valueContexts) {
				//noinspection unchecked
				EventValueContext<E, T> newValueContext = valueContext.buildContext((E) event);
				newEventContext.addEventValueContext(newValueContext);
			}
			activeContexts.put(event, newEventContext);
			return newEventContext;
		}

		/**
		 * Mark this {@link EventContext} as inactive and unable to be accessed.
		 * Should only be used by {@link ch.njol.skript.SkriptEventHandler}.
		 */
		public void offload() {
			assert event != null;
			activeContexts.remove(event);
		}

		public @Nullable E getEvent() {
			return event;
		}

		public Class<E> getEventClass() {
			return eventClass;
		}

		/**
		 * Get all {@link EventValueContext}s stored in this {@link EventContext}
		 * @return
		 */
		public List<EventValueContext<E, T>> getEventValueContexts() {
			return valueContexts;
		}

		/**
		 * Get an {@link EventValueContext} correlating to {@code valueClass}
		 * @param valueClass The class of the expected event value
		 * @return
		 */
		public @Nullable EventValueContext<E, T> getEventValueContext(Class<T> valueClass) {
			for (EventValueContext<E, T> valueContext : valueContexts) {
				if (valueContext.valueClass.equals(valueClass))
					return valueContext;
			}
			return null;
		}

		private void addEventValueContext(EventValueContext<E, T> valueContext) {
			valueContexts.add(valueContext);
		}

		/**
		 * Check if the {@link Event} in this {@link EventContext} is instance of all {@code classes}.
		 * @param classes
		 * @return True if {@link Event} is instance of all {@code classes}
		 */
		@SafeVarargs
		public final boolean instanceOfAll(Class<E>... classes) {
			if (event == null)
				return false;
			for (Class<E> eClass : classes)
				if (!eClass.isInstance(event))
					return false;
			return true;
		}

		/**
		 * Check if the {@link Event} in this {@link EventContext} is instance of any {@code classes}.
		 * @param classes
		 * @return True if {@link Event} is instance of any {@code classes}
		 */
		@SafeVarargs
		public final boolean instanceOfAny(Class<E> ... classes) {
			if (event == null)
				return false;
			for (Class<E> eClass : classes)
				if (eClass.isInstance(event))
					return true;
			return false;
		}

		/**
		 *
		 * @param eClass
		 * @return
		 */
		public @Nullable E getAs(Class<?> eClass) {
			if (event == null)
				return null;
			//noinspection unchecked
			if (!instanceOfAll((Class<E>) eClass))
				return null;
			//noinspection unchecked
			return (E) eClass.cast(event);
		}

		/**
		 * Get the current event value of the {@link EventValueContext} correlating to {@code valueClass}.
		 * If this {@link EventContext} does not have an {@link EventValueContext} for {@code valueClass}, will return null.
		 * If the {@link EventValueContext} has multiple converters, will return null.
		 * @param valueClass The class of the expected event-value
		 * @return
		 */
		public @Nullable T getEventValue(Class<T> valueClass) {
			if (event == null)
				return null;
			EventValueContext<E, T> valueContext = getEventValueContext(valueClass);
			if (valueContext == null || !valueContext.isSingleConverter())
				return null;
			return valueContext.getCurrentValue();
		}

		/**
		 * Get the original event value of the {@link EventValueContext} correlating to {@code valueClass}.
		 * If this {@link EventContext} does not have an {@link EventValueContext} for {@code valueClass}, will return null.
		 * If the {@link EventValueContext} has multiple converters, will return null.
		 * @param valueClass The class of the expected event-value
		 * @return
		 */
		public @Nullable T getOriginalValue(Class<T> valueClass) {
			if (event == null)
				return null;
			EventValueContext<E, T> valueContext = getEventValueContext(valueClass);
			if (valueContext == null || !valueContext.isSingleConverter())
				return null;
			return valueContext.getOriginalValue();
		}

	}

	public static class EventValueContext<E extends Event, T> {
		private E event;
		private final Class<E> eventClass;
		private final Class<T> valueClass;
		private final List<Converter<? super E, ? extends T>> converters;
		private final @Nullable String excludeMessage;
		private final @Nullable Class<? extends E>[] excludedEvents;
		private T originalValue;

		public EventValueContext(
			Class<E> eventClass,
			Class<T> valueClass,
			List<Converter<? super E, ? extends T>> converters
		) {
			this(eventClass, valueClass, converters, null, null);
		}

		public EventValueContext(
			Class<E> eventClass,
			Class<T> valueClass,
			Converter<? super E, ? extends T> converter,
			@Nullable String excludeMessage,
			@Nullable Class<? extends E>[] excludedEvents
		) {
			this(eventClass, valueClass, List.of(converter), excludeMessage, excludedEvents);
		}

		public EventValueContext(
			Class<E> eventClass,
			Class<T> valueClass,
			List<Converter<? super E, ? extends T>> converters,
			@Nullable String excludeMessage,
			@Nullable Class<? extends E>[] excludedEvents
		) {
			this.eventClass = eventClass;
			this.valueClass = valueClass;
			this.converters = converters;
			this.excludeMessage = excludeMessage;
			this.excludedEvents = excludedEvents;
		}

		private EventValueContext(
			E event,
			Class<E> eventClass,
			Class<T> valueClass,
			List<Converter<? super E, ? extends T>> converters,
			@Nullable String excludeMessage,
			@Nullable Class<? extends E>[] excludedEvents
		) {
			this.event = event;
			this.eventClass = eventClass;
			this.valueClass = valueClass;
			this.converters = converters;
			this.excludeMessage = excludeMessage;
			this.excludedEvents = excludedEvents;
			if (isSingleConverter())
				originalValue = converters.get(0).convert(event);
		}

		/**
		 * Builds this {@link EventValueContext} with the provided {@code event} to get the event value if this
		 * {@link EventValueContext} has a singular converter.
		 * @param event An event
		 * @return
		 */
		private EventValueContext<E, T> buildContext(E event) {
			return new EventValueContext<>(event, eventClass, valueClass, converters, excludeMessage, excludedEvents);
		}

		private EventValueContext<E, T> copyContext() {
			return new EventValueContext<>(eventClass, valueClass, converters, excludeMessage, excludedEvents);
		}

		public @Nullable E getEvent() {
			return event;
		}

		public Class<E> getEventClass() {
			return eventClass;
		}

		public Class<T> getValueClass() {
			return valueClass;
		}

		/**
		 * Check if this {@link EventValueContext} has only one converter
		 * @return True if only one converter is present
		 */
		public boolean isSingleConverter() {
			return converters.size() == 1;
		}

		/**
		 * Get the converter of this {@link EventValueContext} if it is singular.
		 * @return
		 */
		public @Nullable Converter<E, T> getConverter() {
			if (isSingleConverter())
				//noinspection unchecked
				return (Converter<E, T>) converters.get(0);
			return null;
		}

		/**
		 * Get all converters for this {@link EventValueContext}
		 * Can store more than 1 allowing for efficient use within {@link #hasMultipleConverters(Class, Class, int)}
		 * @return
		 */
		public List<Converter<? super E, ? extends T>> getConverters() {
			return converters;
		}

		public @Nullable String getExcludeMessage() {
			return excludeMessage;
		}

		public @Nullable Class<? extends E>[] getExcludedEvents() {
			return excludedEvents;
		}

		/**
		 * Get the original event-value of this {@link EventValueContext} when it was built through {@link #buildContext(Event)}
		 * @return
		 */
		public T getOriginalValue() {
			return originalValue;
		}

		/**
		 * Get the current event-value of this {@link EventValueContext}
		 * Can either be the same as {@link #getOriginalValue()} or different if changed through Skript.
		 * @return
		 */
		public @Nullable T getCurrentValue() {
			if (isSingleConverter())
				return converters.get(0).convert(event);
			return null;
		}


		/**
		 * Check to see if the provided {@code checkClass} is excluded from this {@link EventValueContext}
		 * Used within {@link #getEventValueConverters(Class, Class, int, boolean, boolean)} and
		 * {@link #getExactEventValueConverter(Class, Class, int)}
		 *
		 * @param checkClass
		 * @return False if {@code eventClass} is excluded
		 */
		public boolean checkExcluded(Class<?> checkClass) {
			if (excludedEvents == null)
				return true;
			for (Class<? extends E> excluded : excludedEvents) {
				if (excluded != null && excluded.isAssignableFrom(checkClass)) {
					Skript.error(excludeMessage);
					return false;
				}
			}
			return true;
		}

	}

	// --------------------- DEPRECATED --------------------- //

	/**
	 * @deprecated Use {@link #registerEventValue(Class, Class, Converter, int)} instead.
	 */
	@Deprecated(forRemoval = true)
	@SuppressWarnings({"removal"})
	public static <E extends Event, T> void registerEventValue(
		Class<E> eventClass,
		Class<T> valueClass,
		Getter<T, E> getter,
		int time
	) {
		registerEventValue(eventClass, valueClass, (Converter<E, T>) getter, time);
	}

	/**
	 * @deprecated Use {@link #registerEventValue(Class, Class, Converter, int, String, Class[])} instead.
	 */
	@Deprecated(forRemoval = true)
	@SafeVarargs
	@SuppressWarnings({"removal"})
	public static <E extends Event, T> void registerEventValue(
		Class<E> eventClass,
		Class<T> valueClass,
		Getter<T, E> getter,
		int time,
		@Nullable String exlcudeMessage,
		@Nullable Class<? extends E>... excludedEvents
	) {
		registerEventValue(eventClass, valueClass, (Converter<E, T>) getter, time, exlcudeMessage, excludedEvents);
	}

	/**
	 * @deprecated Use {@link Converter} instead.
	 */
	@Deprecated(forRemoval = true)
	@SuppressWarnings({"removal"})
	private static <A, B> @Nullable Getter<B, A> toGetter(Converter<A, B> converter) {
		if (converter == null)
			return null;

		return new Getter<>() {
			@Override
			public @Nullable B get(A arg) {
				return converter.convert(arg);
			}
		};
	}

	/**
	 * @deprecated Use {@link #hasMultipleConverters(Class, Class, int)} instead.
	 */
	@Deprecated(forRemoval = true)
	public static <E extends Event, T> Kleenean hasMultipleGetters(Class<E> eventClass, Class<T> valueClass, int time) {
		return hasMultipleConverters(eventClass, valueClass, time);
	}

	/**
	 * @deprecated Use {@link #getExactEventValueConverter(Class, Class, int)} instead.
	 */
	@Deprecated(forRemoval = true)
	@SuppressWarnings({"removal"})
	public static <E extends Event, T> Getter<? extends T, ? super E> getExactEventValueGetter(Class<E> eventClass, Class<T> valueClass, int time) {
		return toGetter(getExactEventValueConverter(eventClass, valueClass, time));
	}

	/**
	 * @deprecated Use {@link #getEventValueConverter(Class, Class, int)} instead.
	 */
	@Deprecated(forRemoval = true)
	@SuppressWarnings({"removal"})
	public static <E extends Event, T> Getter<? extends T, ? super E> getEventValueGetter(Class<E> eventClass, Class<T> valueClass, int time) {
		return toGetter(getEventValueConverter(eventClass, valueClass, time));
	}

}
