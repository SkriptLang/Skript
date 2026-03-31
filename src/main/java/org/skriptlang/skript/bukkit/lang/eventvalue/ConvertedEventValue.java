package org.skriptlang.skript.bukkit.lang.eventvalue;

import ch.njol.skript.classes.Changer.ChangeMode;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.Converter;
import org.skriptlang.skript.lang.converter.Converters;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Optional;

/**
 * An {@link EventValue} that is a converted version of another event value.
 * <p>
 * This class facilitates event value resolution when the requested event or value types differ from
 * those of the source {@link EventValue}. The following rules apply:
 * <ul>
 *     <li>The {@code ConvertedEvent} must be hierarchically related to the {@code SourceEvent}
 *     (one must be assignable from the other).</li>
 *     <li>The {@code ConvertedValue} does not need to be related to the {@code SourceValue},
 *     provided a {@link Converter} exists to perform the transformation.</li>
 *     <li>{@link Changer}s are supported for the converted value if a reverse converter is available
 *     to map the {@code ConvertedValue} back to the {@code SourceValue}.</li>
 * </ul>
 *
 * @param <SourceEvent> The source event type.
 * @param <ConvertedEvent> The converted event type.
 * @param <SourceValue> The source value type.
 * @param <ConvertedValue> The converted value type.
 */
record ConvertedEventValue<SourceEvent extends Event, ConvertedEvent extends Event, SourceValue, ConvertedValue>(
	Class<ConvertedEvent> eventClass,
	Class<ConvertedValue> valueClass,
	EventValue<SourceEvent, SourceValue> source,
	Converter<SourceValue, ConvertedValue> valueConverter,
	@Nullable Converter<ConvertedValue, SourceValue> reverseConverter
) implements EventValue<ConvertedEvent, ConvertedValue> {

	/**
	 * Creates a new converted event value.
	 * <p>
	 * If the requested {@code eventClass} and {@code valueClass} are already compatible with the source
	 * {@link EventValue}, the source itself is returned. Otherwise, a new {@link ConvertedEventValue}
	 * is created if a valid {@link Converter} exists between the source and target value classes.
	 *
	 * @param eventClass The target event class.
	 * @param valueClass The target value class.
	 * @param source The source event value to convert from.
	 * @param <SourceEvent> The source event type.
	 * @param <ConvertedEvent> The target event type.
	 * @param <SourceValue> The source value type.
	 * @param <ConvertedValue> The target value type.
	 * @return The source event value if it is already assignable to the target classes;
	 * otherwise a new {@link ConvertedEventValue} instance, or {@code null} if no suitable
	 * converter exists between the source and target value classes.
	 */
    public static <SourceEvent extends Event, ConvertedEvent extends Event, SourceValue, ConvertedValue> EventValue<ConvertedEvent, ConvertedValue> newInstance(
		Class<ConvertedEvent> eventClass,
		Class<ConvertedValue> valueClass,
		EventValue<SourceEvent, SourceValue> source
	) {
		if (source.eventClass().isAssignableFrom(eventClass) && valueClass.isAssignableFrom(source.valueClass()))
			//noinspection unchecked
			return (EventValue<ConvertedEvent, ConvertedValue>) source;
		Converter<SourceValue, ConvertedValue> converter = getConverter(source.valueClass(), valueClass);
		if (converter == null)
			return null;
		return new ConvertedEventValue<>(
			eventClass,
			valueClass,
			source,
			converter,
			getConverter(valueClass, source.valueClass())
		);
	}

	private static <F, T> @Nullable Converter<F, T> getConverter(Class<F> from, Class<T> to) {
		if (from.isArray() && to.isArray()) {
			//noinspection rawtypes
			var componentConverter = (Converter) getConverter(from.componentType(), to.componentType());
			if (componentConverter == null)
				return null;
			return obj -> {
				T converted = to.cast(Array.newInstance(to.componentType(), Array.getLength(obj)));
				for (int i = 0, length = Array.getLength(converted); i < length; i++) {
					//noinspection unchecked
					Object convertedObj = componentConverter.convert(Array.get(obj, i));
					if (convertedObj == null)
						return null;
					Array.set(converted, i, convertedObj);
				}
				return converted;
			};
		}
		//noinspection unchecked
		return to.isAssignableFrom(from) ? value -> (T) value : Converters.getConverter(from, to);
	}

	public ConvertedEventValue(
		Class<ConvertedEvent> eventClass,
		Class<ConvertedValue> valueClass,
		EventValue<SourceEvent, SourceValue> source,
		Converter<SourceValue, ConvertedValue> valueConverter,
		@Nullable Converter<ConvertedValue, SourceValue> reverseConverter
	) {
		this.eventClass = eventClass;
		this.valueClass = valueClass;
		this.source = source;
		this.valueConverter = valueConverter;
		this.reverseConverter = reverseConverter == null
			? getConverter(valueClass, source.valueClass())
			: reverseConverter;
	}

	@Override
	public Class<ConvertedEvent> eventClass() {
		return eventClass;
	}

	@Override
	public Class<ConvertedValue> valueClass() {
		return valueClass;
	}

	@Override
	public String @Nullable [] patterns() {
		return source.patterns();
	}

	@Override
	public Validation validate(Class<?> event) {
		return source.validate(event);
	}

	@Override
	public boolean matchesInput(String input) {
		return source.matchesInput(input);
	}

	@Override
	public ConvertedValue get(ConvertedEvent event) {
		return converter().convert(event);
	}

	@Override
	public Converter<ConvertedEvent, ConvertedValue> converter() {
		return event -> {
			if (!source.eventClass().isAssignableFrom(event.getClass()))
				return null;
			SourceValue sourceValue = source.get(source.eventClass().cast(event));
			return valueConverter.convert(sourceValue);
		};
	}

	@Override
	public boolean hasChanger(ChangeMode mode) {
		return source.hasChanger(mode);
	}

	@Override
	public Optional<Changer<ConvertedEvent, ConvertedValue>> changer(ChangeMode mode) {
		return source.changer(mode).map(changer -> (event, value) -> {
			if (!source.eventClass().isAssignableFrom(event.getClass()))
				return;
			if (changer instanceof EventValue.NoValueChanger) {
				changer.change(source.eventClass().cast(event), null);
				return;
			}
			if (reverseConverter == null)
				return;
			SourceValue sourceValue = reverseConverter.convert(value);
			if (sourceValue != null)
				changer.change(source.eventClass().cast(event), sourceValue);
		});
	}

	@Override
	public Time time() {
		return source.time();
	}

	@Override
	public Class<? extends ConvertedEvent> @Nullable [] excludedEvents() {
		Class<? extends SourceEvent>[] excludedEvents = source.excludedEvents();
		if (excludedEvents == null)
			return null;
		//noinspection unchecked
		return Arrays.stream(excludedEvents)
			.filter(eventClass::isAssignableFrom)
			.toArray(Class[]::new);
	}

	@Override
	public @Nullable String excludedErrorMessage() {
		return source.excludedErrorMessage();
	}

	@Override
	public @Nullable <NewEvent extends Event, NewValue> EventValue<NewEvent, NewValue> getConverted(
		Class<NewEvent> newEventClass,
		Class<NewValue> newValueClass
	) {
		return ConvertedEventValue.newInstance(newEventClass, newValueClass, source);
	}

	@Override
	public <NewEvent extends Event, NewValue> EventValue<NewEvent, NewValue> getConverted(
		Class<NewEvent> newEventClass,
		Class<NewValue> newValueClass,
		Converter<ConvertedValue, NewValue> converter,
		@Nullable Converter<NewValue, ConvertedValue> reverseConverter
	) {
		return new ConvertedEventValue<>(newEventClass, newValueClass, this, converter, reverseConverter);
	}

}
