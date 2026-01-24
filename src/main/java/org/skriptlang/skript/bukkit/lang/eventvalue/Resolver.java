package org.skriptlang.skript.bukkit.lang.eventvalue;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.registrations.Classes;
import io.papermc.paper.event.player.PlayerTradeEvent;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.util.ClassUtils;

import javax.swing.text.html.parser.Entity;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A class used to resolve {@link EventValue}s.
 *
 * @param <E> The event type.
 * @param <V> The value type.
 */
class Resolver<E extends Event, V> {

	/**
	 * A comparator factory that creates a comparator that compares {@link EventValue}s based on the distance
	 * of their event class to the given event class.
	 */
	static final EventComparatorFactory EVENT_DISTANCE_COMPARATOR = eventClass ->
		Comparator.comparingInt(ev -> ClassUtils.hierarchyDistance(ev.eventClass(), eventClass));

	/**
	 * A comparator factory that creates a comparator that compares {@link EventValue}s based on the distance
	 * of their event class to the given event class, in both directions, with priority to superclasses.
	 */
	static final EventComparatorFactory BI_EVENT_DISTANCE_COMPARATOR = eventClass ->
		EVENT_DISTANCE_COMPARATOR.create(eventClass)
			.thenComparingInt(ev -> ClassUtils.hierarchyDistance(eventClass, ev.valueClass()));

	/**
	 * A comparator factory that creates a comparator that compares {@link EventValue}s based on the distance
	 * of their event class to the given event class, and the distance of their value class to the given value class.
	 */
	static final EventValueComparatorFactory EVENT_VALUE_DISTANCE_COMPARATOR = (eventClass, valueClass) ->
		BI_EVENT_DISTANCE_COMPARATOR.create(eventClass)
			.thenComparingInt(ev -> ClassUtils.hierarchyDistance(valueClass, ev.valueClass()));

	private final Class<E> eventClass;
	private final @Nullable Class<V> valueClass;
	private final Predicate<EventValue<?, ?>> filter;
	private final Comparator<EventValue<?, ?>> comparator;
	private final Function<EventValue<?, ?>, @Nullable EventValue<E, V>> mapper;
	private final boolean filterMatches;

	private Resolver(
		Class<E> eventClass,
		@Nullable Class<V> valueClass,
		Predicate<EventValue<?, ?>> filter,
		Comparator<EventValue<?, ?>> comparator,
		Function<EventValue<?, ?>, @Nullable EventValue<E, V>> mapper,
		boolean filterMatches
	) {
		this.eventClass = eventClass;
		this.valueClass = valueClass;
		this.filter = filter;
		this.comparator = comparator;
		this.mapper = mapper;
		this.filterMatches = filterMatches;
	}

	/**
	 * Resolves the given list of {@link EventValue}s.
	 *
	 * @param eventValues The event values to resolve.
	 * @return The resolution containing the best candidates.
	 */
	public EventValueRegistry.Resolution<E, V> resolve(List<EventValue<?, ?>> eventValues) {
		List<EventValue<E, V>> best = new ArrayList<>();
		EventValue<?, ?> bestMatch = null;
		for (EventValue<?, ?> eventValue : eventValues) {
			if (!filter.test(eventValue))
				continue;

			switch (eventValue.validate(eventClass)) {
				case INVALID -> {
					continue;
				}
				case ABORT -> {
					return EventValueRegistry.Resolution.error();
				}
			}

			int comparison = bestMatch != null ? comparator.compare(eventValue, bestMatch) : -1;
			if (comparison < 0) {
				//noinspection unchecked
				EventValue<E, V> converted = mapper != null ? mapper.apply(eventValue) : (EventValue<E, V>) eventValue;
				if (converted == null)
					continue;
				best.clear();
				best.add(converted);
				bestMatch = eventValue;
			} else if (comparison == 0) {
				//noinspection unchecked
				EventValue<E, V> converted = mapper != null ? mapper.apply(eventValue) : (EventValue<E, V>) eventValue;
				if (converted == null)
					continue;
				best.add(converted);
			}
		}
		if (valueClass != null && filterMatches)
			return EventValueRegistry.Resolution.of(filterEventValues(valueClass, best));
		return EventValueRegistry.Resolution.of(best);
	}

	/**
	 * Filters the given list of {@link EventValue}s to only include those that match the given value class.
	 * <p>
	 * In this method we can strip converters that are able to be obtainable through their own 'event-classinfo'.
	 * For example, {@link PlayerTradeEvent} has a {@link Player} value (player who traded)
	 * 	and an {@link AbstractVillager} value (villager traded from).
	 * Beforehand, since there is no {@link Entity} value, it was grabbing both values as they both can be cast as an {@link Entity},
	 * 	resulting in a parse error of "multiple entities".
	 * Now, we filter out the values that can be obtained using their own classinfo, such as 'event-player'
	 * 	which leaves us only the {@link AbstractVillager} for 'event-entity'.
	 *
	 * @param valueClass The value class to filter by.
	 * @param eventValues The event values to filter.
	 * @param <E> The event type.
	 * @param <V> The value type.
	 * @return The filtered list of event values.
	 */
	private static <E extends Event, V> List<EventValue<E, V>> filterEventValues(
		Class<V> valueClass,
		List<EventValue<E, V>> eventValues
	) {
		if (eventValues.size() <= 1)
			return eventValues;
		List<EventValue<E, V>> filtered = new ArrayList<>();
		ClassInfo<V> requestedValueClassInfo = Classes.getExactClassInfo(valueClass);
		for (EventValue<E, V> eventValue : eventValues) {
			ClassInfo<V> eventValueClassInfo = Classes.getExactClassInfo(eventValue.valueClass());
			if (eventValueClassInfo != null && !eventValueClassInfo.equals(requestedValueClassInfo))
				continue;
			filtered.add(eventValue);
		}
		return filtered.isEmpty() ? eventValues : filtered;
	}

	/**
	 * Creates a new {@link Builder} for the given event class.
	 *
	 * @param eventClass The event class.
	 * @param <E> The event type.
	 * @param <V> The value type.
	 * @return The builder.
	 */
	static <E extends Event, V> Builder<E, V> builder(Class<E> eventClass) {
		return new Builder<>(eventClass, null);
	}

	/**
	 * Creates a new {@link Builder} for the given event class and value class.
	 *
	 * @param eventClass The event class.
	 * @param valueClass The value class.
	 * @param <E> The event type.
	 * @param <V> The value type.
	 * @return The builder.
	 */
	static <E extends Event, V> Builder<E, V> builder(Class<E> eventClass, Class<V> valueClass) {
		return new Builder<>(eventClass, valueClass);
	}

	/**
	 * A builder for {@link Resolver}.
	 *
	 * @param <E> The event type.
	 * @param <V> The value type.
	 */
	static class Builder<E extends Event, V> {

		private final Class<E> eventClass;
		private final @Nullable Class<V> valueClass;
		private Predicate<EventValue<?, ?>> filter = ev -> true;
		private Comparator<EventValue<?, ?>> comparator = (a, b) -> 0;
		private Function<EventValue<?, ?>, @Nullable EventValue<E, V>> mapper;
		private boolean filterMatches = false;

		Builder(Class<E> eventClass, @Nullable Class<V> valueClass) {
			this.eventClass = eventClass;
			this.valueClass = valueClass;
		}

		/**
		 * Sets the filter.
		 *
		 * @param filter The filter.
		 * @return This builder.
		 */
		public Builder<E, V> filter(Predicate<EventValue<?, ?>> filter) {
			this.filter = filter;
			return this;
		}

		/**
		 * Sets the comparator.
		 *
		 * @param comparator The comparator.
		 * @return This builder.
		 */
		public Builder<E, V> comparator(Comparator<EventValue<?, ?>> comparator) {
			this.comparator = comparator;
			return this;
		}

		/**
		 * Sets the comparator using a factory.
		 *
		 * @param factory The factory.
		 * @return This builder.
		 */
		public Builder<E, V> comparator(EventComparatorFactory factory) {
			this.comparator = factory.create(eventClass);
			return this;
		}

		/**
		 * Sets the comparator using a factory.
		 *
		 * @param factory The factory.
		 * @return This builder.
		 */
		public Builder<E, V> comparator(EventValueComparatorFactory factory) {
			this.comparator = factory.create(eventClass, valueClass);
			return this;
		}

		/**
		 * Sets the mapper.
		 *
		 * @param mapper The mapper.
		 * @return This builder.
		 */
		public Builder<E, V> mapper(Function<EventValue<?, ?>, @Nullable EventValue<E, V>> mapper) {
			this.mapper = mapper;
			return this;
		}

		/**
		 * Sets the filter to match the value class.
		 *
		 * @return This builder.
		 */
		public Builder<E, V> filterMatches() {
			this.filterMatches = true;
			return this;
		}

		/**
		 * Builds the resolver.
		 *
		 * @return The resolver.
		 */
		public Resolver<E, V> build() {
			return new Resolver<>(
				eventClass,
				valueClass,
				filter,
				comparator,
				mapper,
				filterMatches
			);
		}

	}

	/**
	 * A factory for creating comparators based on event classes.
	 */
	@FunctionalInterface
	interface EventComparatorFactory {

		/**
		 * Creates a comparator for the given event class.
		 *
		 * @param eventClass The event class.
		 * @return The comparator.
		 */
		Comparator<EventValue<?, ?>> create(Class<? extends Event> eventClass);

	}

	/**
	 * A factory for creating comparators based on event classes and value classes.
	 */
	@FunctionalInterface
	interface EventValueComparatorFactory {

		/**
		 * Creates a comparator for the given event class and value class.
		 *
		 * @param eventClass The event class.
		 * @param valueClass The value class.
		 * @return The comparator.
		 */
		Comparator<EventValue<?, ?>> create(Class<? extends Event> eventClass, Class<?> valueClass);

	}

}
