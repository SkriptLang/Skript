package org.skriptlang.skript.bukkit.lang.eventvalue;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import com.google.common.base.Preconditions;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.lang.converter.Converter;
import org.skriptlang.skript.lang.converter.Converters;
import org.skriptlang.skript.util.ClassUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

final class EventValueRegistryImpl implements EventValueRegistry {

	private final Skript skript;

	private final Map<EventValue.Time, List<EventValue<?, ?>>> eventValues = new EnumMap<>(EventValue.Time.class);

	private final transient Map<Input<?, ?>, Resolution<?, ?>> eventValuesCache = new ConcurrentHashMap<>();

	public EventValueRegistryImpl(Skript skript) {
		this.skript = skript;
		for (EventValue.Time time : EventValue.Time.values())
			eventValues.put(time, new ArrayList<>());
	}

	@Override
	public <E extends Event> void register(EventValue<E, ?> eventValue) {
		Preconditions.checkNotNull(eventValue, "eventValue");
		if (eventValue instanceof ConvertedEventValue)
			throw new SkriptAPIException("Cannot register a converted event value: " + eventValue);
		if (isRegistered(eventValue)) {
			Skript.warning(eventValue + " is already registered.");
			return;
		}
		List<EventValue<?, ?>> eventValues = eventValues(eventValue.time());
		eventValues.add(eventValue);
		eventValuesCache.clear();
	}

	@Override
	public boolean unregister(EventValue<?, ?> eventValue) {
		boolean removed = eventValues(eventValue.time()).remove(eventValue);
		if (removed)
			eventValuesCache.clear();
		return removed;
	}

	@Override
	public boolean isRegistered(EventValue<?, ?> eventValue) {
		for (EventValue<?, ?> existing : eventValues(eventValue.time())) {
			if (existing.matches(eventValue))
				return true;
		}
		return false;
	}

	@Override
	public boolean isRegistered(Class<? extends Event> eventClass, Class<?> valueClass, EventValue.Time time) {
		for (EventValue<?, ?> eventValue : eventValues(time)) {
			if (eventValue.matches(eventClass, valueClass))
				return true;
		}
		return false;
	}

	@Override
	public <E extends Event, V> Resolution<E, V> resolve(Class<E> eventClass, String identifier) {
		return resolve(eventClass, identifier, EventValue.Time.NOW);
	}

	@Override
	public <E extends Event, V> Resolution<E, V> resolve(Class<E> eventClass, String identifier, EventValue.Time time) {
		return resolve(eventClass, identifier, time, Flags.DEFAULT);
	}

	@Override
	public <E extends Event, V> Resolution<E, V> resolve(
		Class<E> eventClass,
		String identifier,
		EventValue.Time time,
		Flags flags
	) {
		Preconditions.checkNotNull(eventClass, "eventClass");
		Preconditions.checkNotNull(identifier, "identifier");

		if (time == EventValue.Time.NOW)
			flags = flags.without(Flag.FALLBACK_TO_DEFAULT_TIME_STATE); // 'past' and 'future' may use the fallback flag

		var input = Input.of(eventClass, identifier, time, flags);
		//noinspection unchecked
		var resolution = (Resolution<E, V>) eventValuesCache.get(input);
		if (resolution != null)
			return resolution;

		//noinspection unchecked
		resolution = Resolver.<E, V>builder(eventClass)
			.filter(ev -> ClassUtils.isRelatedTo(ev.eventClass(), eventClass) && ev.matchesInput(identifier))
			.comparator(Resolver.EVENT_DISTANCE_COMPARATOR)
			.mapper(ev -> (EventValue<E, V>) ev.getConverted(eventClass, ev.valueClass()))
			.build().resolve(eventValues(time));

		if (resolution.successful()) {
			eventValuesCache.put(input, resolution);
			return resolution;
		}

		if (flags.has(Flag.FALLBACK_TO_DEFAULT_TIME_STATE))
			return resolve(eventClass, identifier, EventValue.Time.NOW, flags);

		resolution = Resolution.empty();
		eventValuesCache.put(input, resolution);
		return resolution;
	}

	@Override
	public <E extends Event, V> Resolution<E, ? extends V> resolve(Class<E> eventClass, Class<V> valueClass) {
		return resolve(eventClass, valueClass, EventValue.Time.NOW);
	}

	@Override
	public <E extends Event, V> Resolution<E, ? extends V> resolve(Class<E> eventClass, Class<V> valueClass, EventValue.Time time) {
		return resolve(eventClass, valueClass, time, Flags.DEFAULT);
	}

	@Override
	public <E extends Event, V> Resolution<E, ? extends V> resolve(
		Class<E> eventClass,
		Class<V> valueClass,
		EventValue.Time time,
		Flags flags
	) {
		Preconditions.checkNotNull(eventClass, "eventClass");
		Preconditions.checkNotNull(valueClass, "valueClass");

		if (time == EventValue.Time.NOW)
			flags = flags.without(Flag.FALLBACK_TO_DEFAULT_TIME_STATE); // only 'past' and 'future' may use the fallback flag

		var input = Input.of(eventClass, valueClass, time, flags);
		//noinspection unchecked
		var resolution = (Resolution<E, ? extends V>) eventValuesCache.get(input);
		if (resolution != null)
			return resolution;

		resolution = resolveExact(eventClass, valueClass, time)
			.anyOptional()
			.map(eventValue -> Resolution.of(Collections.singletonList(eventValue)))
			.orElse(Resolution.empty());
		if (resolution.successful() || resolution.errored()) {
			eventValuesCache.put(input, resolution);
			return resolution;
		}

		resolution = resolveNearest(eventClass, valueClass, time);
		if (resolution.successful() || resolution.errored()) {
			eventValuesCache.put(input, resolution);
			return resolution;
		}

		if (flags.has(Flag.ALLOW_CONVERSION)) {
			resolution = resolveWithDowncastConversion(eventClass, valueClass, time);
			if (resolution.successful() || resolution.errored()) {
				eventValuesCache.put(input, resolution);
				return resolution;
			}

			resolution = resolveWithConversion(eventClass, valueClass, time);
			if (resolution.successful() || resolution.errored()) {
				eventValuesCache.put(input, resolution);
				return resolution;
			}
		}

		if ((flags.has(Flag.FALLBACK_TO_DEFAULT_TIME_STATE)))
			return resolve(eventClass, valueClass, EventValue.Time.NOW, flags);

		resolution = Resolution.empty();
		eventValuesCache.put(input, resolution);
		return resolution;
	}

	@Override
	public <E extends Event, V> Resolution<E, V> resolveExact(
		Class<E> eventClass,
		Class<V> valueClass,
		EventValue.Time time
	) {
		return Resolver.builder(eventClass, valueClass)
			.filter(ev -> ev.eventClass().isAssignableFrom(eventClass) && ev.valueClass().equals(valueClass))
			.comparator(Resolver.EVENT_DISTANCE_COMPARATOR)
			.filterMatches()
			.build().resolve(eventValues(time));
	}

	/**
	 * Resolves to the nearest event and value class without conversion.
	 */
	private <E extends Event, V> Resolution<E, ? extends V> resolveNearest(
		Class<E> eventClass,
		Class<V> valueClass,
		EventValue.Time time
	) {
		return Resolver.builder(eventClass, valueClass)
			.filter(ev -> ClassUtils.isRelatedTo(ev.eventClass(), eventClass) && valueClass.isAssignableFrom(ev.valueClass()))
			.comparator(Resolver.EVENT_VALUE_DISTANCE_COMPARATOR)
			.mapper(ev -> ev.getConverted(eventClass, valueClass))
			.filterMatches()
			.build().resolve(eventValues(time));
	}

	/**
	 * Resolves using downcast conversion when the desired value class is a supertype
	 * of the registered value class.
	 */
	private <E extends Event, V> Resolution<E, V> resolveWithDowncastConversion(
		Class<E> eventClass,
		Class<V> valueClass,
		EventValue.Time time
	) {
		Converter<?, V> converter = source -> valueClass.isInstance(source) ? valueClass.cast(source) : null;
		//noinspection unchecked,rawtypes
		return Resolver.builder(eventClass, valueClass)
			.filter(ev -> ClassUtils.isRelatedTo(ev.eventClass(), eventClass)
				&& ev.valueClass().isAssignableFrom(valueClass))
			.comparator(Resolver.EVENT_VALUE_DISTANCE_COMPARATOR)
			.mapper(ev -> ev.getConverted(eventClass, valueClass, (Converter) converter))
			.filterMatches()
			.build().resolve(eventValues(time));
	}

	/**
	 * Resolves using {@link Converters} to convert value type when needed.
	 */
	private <E extends Event, V> Resolution<E, V> resolveWithConversion(
		Class<E> eventClass,
		Class<V> valueClass,
		EventValue.Time time
	) {
		return Resolver.builder(eventClass, valueClass)
			.filter(ev -> ClassUtils.isRelatedTo(ev.eventClass(), eventClass))
			.comparator(Resolver.BI_EVENT_DISTANCE_COMPARATOR)
			.mapper(ev -> ev.getConverted(eventClass, valueClass))
			.build().resolve(eventValues(time));
	}

	private List<EventValue<?, ?>> eventValues(EventValue.Time time) {
		return eventValues.get(time);
	}

	@Override
	public @Unmodifiable List<EventValue<?, ?>> elements() {
		return eventValues.values().stream()
			.flatMap(List::stream)
			.toList();
	}

	@Override
	public @Unmodifiable List<EventValue<?, ?>> elements(EventValue.Time time) {
		return List.copyOf(eventValues(time));
	}

	@Override
	public @Unmodifiable <E extends Event> List<EventValue<? extends E, ?>> elements(Class<E> event) {
		//noinspection unchecked,rawtypes
		return (List) eventValues.values().stream()
			.flatMap(List::stream)
			.filter(eventValue -> event.isAssignableFrom(eventValue.eventClass()))
			.toList();
	}

	private record Input<E extends Event, I>(
		Class<E> eventClass,
		I input,
		EventValue.Time time,
		Flags flags
	) {
		static <E extends Event> Input<E, String> of(
			Class<E> eventClass,
			String input,
			EventValue.Time time,
			Flags flags
		) {
			return new Input<>(eventClass, input, time, flags);
		}

		static <E extends Event> Input<E, Class<?>> of(
			Class<E> eventClass,
			Class<?> input,
			EventValue.Time time,
			Flags flags
		) {
			return new Input<>(eventClass, input, time, flags);
		}
	}

	static class UnmodifiableView implements EventValueRegistry {

		private final EventValueRegistry delegate;

		UnmodifiableView(EventValueRegistry delegate) {
			this.delegate = delegate;
		}

		@Override
		public <E extends Event> void register(EventValue<E, ?> eventValue) {
			throw new UnsupportedOperationException("Cannot register event values with an unmodifiable event value registry.");
		}

		@Override
		public boolean unregister(EventValue<?, ?> eventValue) {
			throw new UnsupportedOperationException("Cannot unregister event values from an unmodifiable event value registry.");
		}

		@Override
		public boolean isRegistered(EventValue<?, ?> eventValue) {
			return delegate.isRegistered(eventValue);
		}

		@Override
		public boolean isRegistered(Class<? extends Event> eventClass, Class<?> valueClass, EventValue.Time time) {
			return delegate.isRegistered(eventClass, valueClass, time);
		}

		@Override
		public <E extends Event, V> Resolution<E, V> resolve(Class<E> eventClass, String identifier) {
			return delegate.resolve(eventClass, identifier);
		}

		@Override
		public <E extends Event, V> Resolution<E, V> resolve(Class<E> eventClass, String identifier, EventValue.Time time) {
			return delegate.resolve(eventClass, identifier, time);
		}

		@Override
		public <E extends Event, V> Resolution<E, V> resolve(Class<E> eventClass, String identifier, EventValue.Time time, Flags flags) {
			return delegate.resolve(eventClass, identifier, time, flags);
		}

		@Override
		public <E extends Event, V> Resolution<E, ? extends V> resolve(Class<E> eventClass, Class<V> valueClass) {
			return delegate.resolve(eventClass, valueClass);
		}

		@Override
		public <E extends Event, V> Resolution<E, ? extends V> resolve(Class<E> eventClass, Class<V> valueClass, EventValue.Time time) {
			return delegate.resolve(eventClass, valueClass, time);
		}

		@Override
		public <E extends Event, V> Resolution<E, ? extends V> resolve(Class<E> eventClass, Class<V> valueClass, EventValue.Time time, Flags flags) {
			return delegate.resolve(eventClass, valueClass, time, flags);
		}

		@Override
		public <E extends Event, V> Resolution<E, V> resolveExact(Class<E> eventClass, Class<V> valueClass, EventValue.Time time) {
			return delegate.resolveExact(eventClass, valueClass, time);
		}

		@Override
		public @Unmodifiable List<EventValue<?, ?>> elements() {
			return delegate.elements();
		}

		@Override
		public @Unmodifiable List<EventValue<?, ?>> elements(EventValue.Time time) {
			return delegate.elements(time);
		}

		@Override
		public @Unmodifiable <E extends Event> List<EventValue<? extends E, ?>> elements(Class<E> event) {
			return delegate.elements(event);
		}

		@Override
		public EventValueRegistry unmodifiableView() {
			return this;
		}

	}

}
