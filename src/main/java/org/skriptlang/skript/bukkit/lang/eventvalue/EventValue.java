package org.skriptlang.skript.bukkit.lang.eventvalue;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.Converter;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * Describes a single "event value" available in a specific {@link org.bukkit.event.Event} context.
 * An event value provides a typed value (e.g. the player, entity, location) for a given event and
 * can optionally support changing that value via Skript's {@link ch.njol.skript.classes.Changer} API.
 * <p>
 * Each event value is identified by one or more textual identifier patterns that are matched against
 * user input (e.g. {@code "player"}, {@code "entity"}). Resolution and lookup are handled by
 * {@link EventValueRegistry}.
 * <p>
 * Instances should be created using {@link #builder(Class, Class)} and registered via
 * {@link EventValueRegistry#register(EventValue)}.
 */
public sealed interface EventValue<E extends Event, V> permits EventValueImpl, ConvertedEventValue {

	/**
	 * Creates a new builder for an {@link EventValue}.
	 *
	 * @param eventClass the event type this value applies to
	 * @param valueClass the value type to produce
	 * @return a builder to configure and build the event value
	 * @see #simple(Class, Class, Converter)
	 */
	static <E extends Event, V> EventValue.Builder<E, V> builder(Class<E> eventClass, Class<V> valueClass) {
		return new EventValueImpl.BuilderImpl<>(eventClass, valueClass);
	}

	/**
	 * Creates a new {@link EventValue} using a getter
	 *
	 * @param eventClass the event type this value applied to
	 * @param valueClass the value type to produce
	 * @param converter the getter
	 * @return the constructed event value
	 * @see #builder(Class, Class)
	 */
	static <E extends Event, V> EventValue<E, V> simple(Class<E> eventClass, Class<V> valueClass, Converter<E, V> converter) {
		return builder(eventClass, valueClass).getter(converter).build();
	}

	/**
	 * The event class this value applies to.
	 *
	 * @return the event type this event value is defined for
	 */
	Class<E> eventClass();

	/**
	 * The type of the value produced by this event value.
	 *
	 * @return the value type
	 */
	Class<V> valueClass();

	/**
	 * Patterns used to identify this event value from user input.
	 *
	 * @return the patterns
	 */
	String @Nullable [] patterns();

	/**
	 * Validates that this event value can be used in the provided event context.
	 *
	 * @param event the concrete event class to validate against
	 * @return the validation status
	 */
	Validation validate(Class<?> event);

	/**
	 * Checks whether the provided input matches one of {@link #patterns()} and
	 * satisfies any additional input validation.
	 *
	 * @param input the identifier provided by the user
	 * @return {@code true} if the validation succeeds
	 */
	boolean matchesInput(String input);

	/**
	 * Obtains the value from the given event instance.
	 *
	 * @param event the event instance
	 * @return the value obtained from the event, which may be {@code null}
	 */
	V get(E event);

	/**
	 * The converter used to obtain the value from the event.
	 *
	 * @return the converter
	 */
	Converter<E, V> converter();

	/**
	 * Checks whether a changer is supported for the specified {@link ChangeMode}.
	 *
	 * @param mode the change mode
	 * @return {@code true} if a changer is supported
	 */
	boolean hasChanger(ChangeMode mode);

	/**
	 * Returns the changer for the specified {@link ChangeMode}, if present.
	 *
	 * @param mode the change mode
	 * @return an {@link Optional} containing the changer if available
	 */
	Optional<Changer<E, V>> changer(ChangeMode mode);

	/**
	 * The time state this event value is registered for.
	 *
	 * @return the time state
	 */
	Time time();

	/**
	 * Event types explicitly excluded from using this event value.
	 *
	 * @return an array of excluded event classes or {@code null} if none
	 */
	Class<? extends E> @Nullable [] excludedEvents();

	/**
	 * An optional error message shown when this value is excluded for a matching event.
	 *
	 * @return the exclusion error message or {@code null}
	 */
	@Nullable String excludedErrorMessage();

	/**
	 * Checks whether this event value matches the provided event value in terms of
	 * event class, value class, and identifier patterns.
	 *
	 * @param eventValue the event value to compare against
	 * @return {@code true} if they match
	 */
	default boolean matches(EventValue<?, ?> eventValue) {
		return matches(eventValue.eventClass(), eventValue.valueClass(), eventValue.patterns());
	}

	/**
	 * Checks whether this event value matches the provided event class, value class,
	 * and identifier patterns.
	 *
	 * @param eventClass the event class to compare against
	 * @param valueClass the value class to compare against
	 * @param patterns the patterns to compare against
	 * @return {@code true} if they match
	 */
	default boolean matches(Class<? extends Event> eventClass, Class<?> valueClass, String[] patterns) {
		return matches(eventClass, valueClass) && Arrays.equals(patterns(), patterns);
	}

	/**
	 * Checks whether this event value matches the provided event class and value class.
	 *
	 * @param eventClass the event class to compare against
	 * @param valueClass the value class to compare against
	 * @return {@code true} if they match
	 */
	default boolean matches(Class<? extends Event> eventClass, Class<?> valueClass) {
		return eventClass().equals(eventClass) && valueClass().equals(valueClass);
	}

	/**
	 * Returns a new event value that converts this value to a different value class,
	 * or uses a different event class.
	 * <p>
	 * This method attempts to find a suitable converter for the value classes automatically.
	 *
	 * @param newEventClass the new event class
	 * @param newValueClass the new value class
	 * @param <ConvertedEvent> the new event type
	 * @param <ConvertedValue> the new value type
	 * @return a new converted event value, or {@code null} if no converter was found
	 */
	@Nullable <ConvertedEvent extends Event, ConvertedValue> EventValue<ConvertedEvent, ConvertedValue> getConverted(
		Class<ConvertedEvent> newEventClass,
		Class<ConvertedValue> newValueClass
	);

	/**
	 * Returns a new event value that converts this value to a different value class
	 * using the provided converter.
	 *
	 * @param newEventClass the new event class
	 * @param newValueClass the new value class
	 * @param converter the converter to use
	 * @param <NewEvent> the new event type
	 * @param <NewValue> the new value type
	 * @return a new converted event value
	 * @see #getConverted(Class, Class, Converter, Converter)
	 */
	default @Nullable <NewEvent extends Event, NewValue> EventValue<NewEvent, NewValue> getConverted(
		Class<NewEvent> newEventClass,
		Class<NewValue> newValueClass,
		Converter<V, NewValue> converter
	) {
		return getConverted(newEventClass, newValueClass, converter, null);
	}

	/**
	 * Returns a new event value that converts this value to a different value class
	 * using the provided converter and reverse converter.
	 *
	 * @param newEventClass the new event class
	 * @param newValueClass the new value class
	 * @param converter the converter to use to obtain the new value type
	 * @param reverseConverter the reverse converter to use for changing the value, if available
	 * @param <NewEvent> the new event type
	 * @param <NewValue> the new value type
	 * @return a new converted event value
	 */
	<NewEvent extends Event, NewValue> EventValue<NewEvent, NewValue> getConverted(
		Class<NewEvent> newEventClass,
		Class<NewValue> newValueClass,
		Converter<V, NewValue> converter,
		@Nullable Converter<NewValue, V> reverseConverter
	);

	/**
	 * Represents the time state an event value is registered for.
	 */
	enum Time {
		/**
		 * The value as it was in the past (e.g. before the event occurred).
		 */
		PAST(-1),
		/**
		 * The value as it is now (during the event).
		 */
		NOW(0),
		/**
		 * The value as it will be in the future (e.g. after the event).
		 */
		FUTURE(1);

		private final int value;

		Time(int value) {
			this.value = value;
		}

		/**
		 * @return the integer value of this time state
		 */
		public int value() {
			return value;
		}

		/**
		 * Obtains the time state from its integer value.
		 *
		 * @param value the integer value
		 * @return the corresponding time state
		 * @throws IllegalArgumentException if the value is invalid
		 */
		public static Time of(int value) {
			return switch (value) {
				case -1 -> PAST;
				case 0 -> NOW;
				case 1 -> FUTURE;
				default -> throw new IllegalArgumentException("Invalid time value: " + value);
			};
		}

	}

	/**
	 * Represents the validation status of an event value against an event context.
	 */
	enum Validation {
		/**
		 * The event value is valid for the given event context.
		 */
		VALID,
		/**
		 * The event value is invalid for the given event context and should be ignored.
		 */
		INVALID,
		/**
		 * The event value is invalid for the given event context and indicates an error
		 * that should stop the resolution process.
		 */
		ABORT,
	}

	/**
	 * A changer for applying modifications to the value for a given event instance.
	 *
	 * @param <E> the event type
	 * @param <V> the value type
	 */
	@FunctionalInterface
	interface Changer<E extends Event, V> {

		/**
		 * Applies a change to the value for the given event instance.
		 *
		 * @param event the event instance
		 * @param value the value to apply (may be {@code null} depending on mode)
		 */
		void change(E event, V value);

	}

	/**
	 * A changer that does not require a value to be passed (e.g. for {@link ChangeMode#DELETE} or {@link ChangeMode#RESET}).
	 *
	 * @param <E> the event type
	 * @param <V> the value type
	 */
	@FunctionalInterface
	interface NoValueChanger<E extends Event, V> extends Changer<E, V> {

		/**
		 * Applies a change to the given event instance without a value.
		 *
		 * @param event the event instance
		 */
		void change(E event);

		/**
		 * {@inheritDoc}
		 * <p>
		 * This implementation ignores the provided value and calls {@link #change(Event)}.
		 *
		 * @param event the event instance
		 * @param value the value (ignored)
		 */
		@Override
		default void change(E event, V value) {
			change(event);
		}

	}

	/**
	 * A builder for creating {@link EventValue} instances.
	 *
	 * @param <E> the event type
	 * @param <V> the value type
	 */
	interface Builder<E extends Event, V> {

		/**
		 * Adds one or more patterns matched against user input.
		 *
		 * @param patterns Skript patterns
		 * @return this builder
		 */
		@Contract(value = "_ -> this", mutates = "this")
		Builder<E,V> patterns(String... patterns);

		/**
		 * Sets an additional validator that must accept the input and parse result for this value to match.
		 *
		 * @param inputValidator predicate invoked after pattern match
		 * @return this builder
		 */
		@Contract(value = "_ -> this", mutates = "this")
		Builder<E,V> inputValidator(BiPredicate<String, ParseResult> inputValidator);

		/**
		 * Sets an event-type validator that must accept the event class for this value to be valid.
		 *
		 * @param eventValidator predicate to validate event classes
		 * @return this builder
		 */
		@Contract(value = "_ -> this", mutates = "this")
		Builder<E,V> eventValidator(Function<Class<?>, Validation> eventValidator);

		/**
		 * Sets the converter used to obtain the value from the event.
		 *
		 * @param converter the value converter
		 * @return this builder
		 */
		@Contract(value = "_ -> this", mutates = "this")
		Builder<E,V> getter(Converter<E, V> converter);

		/**
		 * Registers a changer for the event value.
		 *
		 * @param mode the change mode
		 * @param changer the changer implementation
		 * @return this builder
		 */
		@Contract(value = "_, _ -> this", mutates = "this")
		Builder<E,V> registerChanger(ChangeMode mode, Changer<E, V> changer);

		/**
		 * Sets the time state for which this event value is registered.
		 *
		 * @param time the time state
		 * @return this builder
		 */
		@Contract(value = "_ -> this", mutates = "this")
		Builder<E,V> time(Time time);

		/**
		 * Excludes a specific event subclass from using this event value.
		 *
		 * @param event event class to exclude
		 * @return this builder
		 */
		@Contract(value = "_ -> this", mutates = "this")
		default Builder<E, V> excludes(Class<? extends E> event) {
			excludes(CollectionUtils.array(event));
			return this;
		}

		/**
		 * Excludes specific event subclasses from using this event value.
		 *
		 * @param event1 first event class to exclude
		 * @param event2 second event class to exclude
		 * @return this builder
		 */
		@Contract(value = "_, _ -> this", mutates = "this")
		default Builder<E, V> excludes(Class<? extends E> event1, Class<? extends E> event2) {
			excludes(CollectionUtils.array(event1, event2));
			return this;
		}

		/**
		 * Excludes specific event subclasses from using this event value.
		 *
		 * @param event1 first event class to exclude
		 * @param event2 second event class to exclude
		 * @param event3 third event class to exclude
		 * @return this builder
		 */
		@Contract(value = "_, _, _ -> this", mutates = "this")
		default Builder<E, V> excludes(Class<? extends E> event1, Class<? extends E> event2, Class<? extends E> event3) {
			excludes(CollectionUtils.array(event1, event2, event3));
			return this;
		}

		/**
		 * Excludes specific event subclasses from using this event value.
		 *
		 * @param events event classes to exclude
		 * @return this builder
		 */
		@Contract(value = "_ -> this", mutates = "this")
		Builder<E, V> excludes(Class<? extends E>[] events);

		/**
		 * Sets an error message to be shown if this event value is selected for an excluded event.
		 *
		 * @param excludedErrorMessage the message to display
		 * @return this builder
		 */
		@Contract(value = "_ -> this", mutates = "this")
		Builder<E, V> excludedErrorMessage(String excludedErrorMessage);

		/**
		 * Builds the event value.
		 *
		 * @return the constructed event value instance
		 */
		EventValue<E, V> build();

	}

}
