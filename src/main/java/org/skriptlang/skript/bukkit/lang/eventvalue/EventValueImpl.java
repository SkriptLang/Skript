package org.skriptlang.skript.bukkit.lang.eventvalue;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Utils;
import com.google.common.base.MoreObjects;
import org.bukkit.event.Event;
import org.intellij.lang.annotations.RegExp;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.Converter;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Default implementation of {@link EventValue}.
 */
final class EventValueImpl<E extends Event, V> implements EventValue<E, V> {

	private final Class<E> eventClass;
	private final Class<V> valueClass;
	private final Pattern[] identifierPatterns;
	private final @Nullable Predicate<String> inputValidator;
	private final @Nullable Function<Class<?>, Validation> eventValidator;
	private final Converter<E, V> converter;
	private final Map<ChangeMode, Changer<E, V>> changers;
	private final Time time;
	private final Class<? extends E> @Nullable [] excludedEvents;
	private final @Nullable String excludedErrorMessage;

	EventValueImpl(
		Class<E> eventClass,
		Class<V> valueClass,
		Pattern[] identifierPatterns,
		@Nullable Predicate<String> inputValidator,
		@Nullable Function<Class<?>, Validation> eventValidator,
		Converter<E, V> converter,
		Map<ChangeMode, Changer<E, V>> changers,
		Time time,
		Class<? extends E> @Nullable [] excludedEvents,
		@Nullable String excludedErrorMessage
	) {
		this.eventClass = eventClass;
		this.valueClass = valueClass;
		this.identifierPatterns = identifierPatterns;
		this.inputValidator = inputValidator;
		this.eventValidator = eventValidator;
		this.converter = converter;
		this.changers = changers;
		this.time = time;
		this.excludedEvents = excludedEvents;
		this.excludedErrorMessage = excludedErrorMessage;
	}

	@Override
	public Class<E> eventClass() {
		return eventClass;
	}

	@Override
	public Class<V> valueClass() {
		return valueClass;
	}

	@Override
	public Pattern[] identifierPatterns() {
		return identifierPatterns.clone();
	}

	@Override
	public Validation validate(Class<?> event) {
		if (excludedEvents != null) {
			for (Class<? extends E> excludedEvent : excludedEvents) {
				if (!excludedEvent.isAssignableFrom(event))
					continue;
				if (excludedErrorMessage != null)
					Skript.error(excludedErrorMessage);
				return Validation.ABORT;
			}
		}
		if (eventValidator == null)
			return Validation.VALID;
		return eventValidator.apply(event);
	}

	@Override
	public boolean matchesInput(String input) {
		for (Pattern pattern : identifierPatterns) {
			if (pattern.matcher(input).matches())
				return inputValidator == null || inputValidator.test(input);
		}
		return false;
	}

	@Override
	public V get(E event) {
		return converter.convert(event);
	}

	@Override
	public Converter<E, V> converter() {
		return converter;
	}

	@Override
	public boolean hasChanger(ChangeMode mode) {
		return changers.containsKey(mode);
	}

	@Override
	public Optional<Changer<E, V>> changer(ChangeMode mode) {
		return Optional.ofNullable(changers.get(mode));
	}

	@Override
	public Time time() {
		return time;
	}

	@Override
	public Class<? extends E> @Nullable [] excludedEvents() {
		return excludedEvents != null ? excludedEvents.clone() : null;
	}

	@Override
	public @Nullable String excludedErrorMessage() {
		return excludedErrorMessage;
	}

	@Override
	public @Nullable <ConvertedEvent extends Event, ConvertedValue> EventValue<ConvertedEvent, ConvertedValue> getConverted(
		Class<ConvertedEvent> newEventClass,
		Class<ConvertedValue> newValueClass
	) {
		return ConvertedEventValue.newInstance(newEventClass, newValueClass, this);
	}

	@Override
	public <ConvertedEvent extends Event, ConvertedValue> EventValue<ConvertedEvent, ConvertedValue> getConverted(
		Class<ConvertedEvent> newEventClass,
		Class<ConvertedValue> newValueClass,
		Converter<V, ConvertedValue> converter,
		@Nullable Converter<ConvertedValue, V> reverseConverter
	) {
		return new ConvertedEventValue<>(newEventClass, newValueClass, this, converter, reverseConverter);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("eventClass", eventClass)
			.add("valueClass", valueClass)
			.add("identifierPatterns", identifierPatterns)
			.add("time", time)
			.toString();
	}

	static class BuilderImpl<E extends Event, V> implements Builder<E, V> {

		private final Class<E> eventClass;
		private final Class<V> valueClass;
		private final Map<ChangeMode, Changer<E, V>> changers = new EnumMap<>(ChangeMode.class);
		private String @Nullable [] identifierPatterns;
		private @Nullable Predicate<String> inputValidator;
		private @Nullable Function<Class<?>, Validation> eventValidator;
		private Converter<E, V> converter;
		private Time time = Time.NOW;
		private Class<? extends E> @Nullable [] excludedEvents;
		private @Nullable String excludedErrorMessage;

		BuilderImpl(Class<E> eventClass, Class<V> valueClass) {
			this.eventClass = eventClass;
			this.valueClass = valueClass;
		}

		@Override
		@Contract(value = "_ -> this", mutates = "this")
		public Builder<E,V> identifierPattern(@RegExp String... identifierPattern) {
			this.identifierPatterns = identifierPattern;
			return this;
		}

		@Override
		@Contract(value = "_ -> this", mutates = "this")
		public Builder<E,V> inputValidator(Predicate<String> inputValidator) {
			this.inputValidator = inputValidator;
			return this;
		}

		@Override
		@Contract(value = "_ -> this", mutates = "this")
		public Builder<E,V> eventValidator(Function<Class<?>, Validation> eventValidator) {
			this.eventValidator = eventValidator;
			return this;
		}

		@Override
		@Contract(value = "_ -> this", mutates = "this")
		public Builder<E,V> getter(Converter<E, V> converter) {
			this.converter = converter;
			return this;
		}

		@Override
		@Contract(value = "_ -> this", mutates = "this")
		public Builder<E,V> registerSetChanger(Changer<E, V> changer) {
			changers.put(ChangeMode.SET, changer);
			return this;
		}

		@Override
		@Contract(value = "_ -> this", mutates = "this")
		public Builder<E,V> registerAddChanger(Changer<E, V> changer) {
			changers.put(ChangeMode.ADD, changer);
			return this;
		}

		@Override
		@Contract(value = "_ -> this", mutates = "this")
		public Builder<E,V> registerRemoveChanger(Changer<E, V> changer) {
			changers.put(ChangeMode.REMOVE, changer);
			return this;
		}

		@Override
		@Contract(value = "_ -> this", mutates = "this")
		public Builder<E,V> registerRemoveAllChanger(Changer<E, V> changer) {
			changers.put(ChangeMode.REMOVE_ALL, changer);
			return this;
		}

		@Override
		@Contract(value = "_ -> this", mutates = "this")
		public Builder<E,V> registerDeleteChanger(NoValueChanger<E, V> changer) {
			changers.put(ChangeMode.DELETE, changer);
			return this;
		}

		@Override
		@Contract(value = "_ -> this", mutates = "this")
		public Builder<E,V> registerResetChanger(NoValueChanger<E, V> changer) {
			changers.put(ChangeMode.RESET, changer);
			return this;
		}

		@Override
		@Contract(value = "_ -> this", mutates = "this")
		public Builder<E,V> time(Time time) {
			this.time = time;
			return this;
		}

		@Override
		@SafeVarargs
		@Contract(value = "_ -> this", mutates = "this")
		public final Builder<E, V> excludes(Class<? extends E>... events) {
			this.excludedEvents = events;
			return this;
		}

		@Override
		@Contract(value = "_ -> this", mutates = "this")
		public Builder<E, V> excludedErrorMessage(String excludedErrorMessage) {
			this.excludedErrorMessage = excludedErrorMessage;
			return this;
		}

		@Override
		public EventValue<E, V> build() {
			Pattern[] identifierPatterns;
			if (this.identifierPatterns == null) {
				boolean plural = valueClass.isArray();
				//noinspection unchecked
				ClassInfo<?> type = Classes.getExactClassInfo(plural ? (Class<V>) valueClass.getComponentType() : valueClass);
				if (type != null && type.getUserInputPatterns() != null) {
					identifierPatterns = type.getUserInputPatterns();
					inputValidator = combinePredicates(
						input -> plural == Utils.getEnglishPlural(input).getSecond(),
						inputValidator
					);
				} else {
					String className = plural
						? Utils.toEnglishPlural(valueClass.getComponentType().getSimpleName())
						: valueClass.getSimpleName();
					identifierPatterns = new Pattern[] {Pattern.compile(className.toLowerCase(Locale.ENGLISH))};
				}
			} else {
				identifierPatterns = new Pattern[this.identifierPatterns.length];
				for (int i = 0; i < this.identifierPatterns.length; i++)
					identifierPatterns[i] = Pattern.compile(this.identifierPatterns[i]);
			}

			return new EventValueImpl<>(
				eventClass,
				valueClass,
				identifierPatterns,
				inputValidator,
				eventValidator,
				converter,
				changers,
				time,
				excludedEvents,
				excludedErrorMessage
			);
		}

		private static <V> Predicate<V> combinePredicates(
			@Nullable Predicate<V> first,
			@Nullable Predicate<V> second
		) {
			if (first == null)
				return second;
			if (second == null)
				return first;
			return first.and(second);
		}

	}

}
