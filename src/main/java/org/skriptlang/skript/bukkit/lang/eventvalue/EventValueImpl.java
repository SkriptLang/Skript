package org.skriptlang.skript.bukkit.lang.eventvalue;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.patterns.MatchResult;
import ch.njol.skript.patterns.PatternCompiler;
import ch.njol.skript.patterns.RegexPatternElement;
import ch.njol.skript.patterns.SkriptPattern;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Utils;
import com.google.common.base.MoreObjects;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.Converter;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * Default implementation of {@link EventValue}.
 */
final class EventValueImpl<E extends Event, V> implements EventValue<E, V> {

	private final Class<E> eventClass;
	private final Class<V> valueClass;
	private final String @Nullable [] patterns;
	private final @Nullable BiPredicate<String, ParseResult> inputValidator;
	private final @Nullable Function<Class<?>, Validation> eventValidator;
	private final Converter<E, V> converter;
	private final Map<ChangeMode, Changer<E, V>> changers;
	private final Time time;
	private final Class<? extends E> @Nullable [] excludedEvents;
	private final @Nullable String excludedErrorMessage;

	private SkriptPattern[] compiledPatterns;

	EventValueImpl(
		Class<E> eventClass,
		Class<V> valueClass,
		String @Nullable [] patterns,
		@Nullable BiPredicate<String, ParseResult> inputValidator,
		@Nullable Function<Class<?>, Validation> eventValidator,
		Converter<E, V> converter,
		Map<ChangeMode, Changer<E, V>> changers,
		Time time,
		Class<? extends E> @Nullable [] excludedEvents,
		@Nullable String excludedErrorMessage
	) {
		this.eventClass = eventClass;
		this.valueClass = valueClass;
		this.patterns = patterns;
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
	public String @Nullable [] patterns() {
		return patterns != null ? patterns.clone() : null;
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
		for (SkriptPattern pattern : compilePatterns()) {
			MatchResult match = pattern.match(input);
			if (match != null && (inputValidator == null || inputValidator.test(input, match.toParseResult())))
				return true;
		}
		return false;
	}

	private SkriptPattern[] compilePatterns() {
		if (compiledPatterns != null)
			return compiledPatterns;
		compiledPatterns = patterns == null ? patternsFromType(valueClass) : Arrays.stream(patterns)
			.map(PatternCompiler::compile)
			.toArray(SkriptPattern[]::new);
		return compiledPatterns;
	}

	private SkriptPattern[] patternsFromType(Class<?> type) {
		boolean plural = type.isArray();
		if (plural)
			type = type.componentType();

		ClassInfo<?> info = Classes.getExactClassInfo(type);
		if (info == null || info.getUserInputPatterns() == null) {
			String name = type.getSimpleName().toLowerCase(Locale.ENGLISH);
			return new SkriptPattern[] {PatternCompiler.compile(Utils.toEnglishPlural(name, plural))};
		}

		return Arrays.stream(info.getUserInputPatterns())
			.map(RegexPatternElement::new)
			.map(pattern -> new SkriptPattern(pattern, 0))
			.toArray(SkriptPattern[]::new);
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
			.add("patterns", patterns)
			.add("time", time)
			.toString();
	}

	static class BuilderImpl<E extends Event, V> implements Builder<E, V> {

		private final Class<E> eventClass;
		private final Class<V> valueClass;
		private final Map<ChangeMode, Changer<E, V>> changers = new EnumMap<>(ChangeMode.class);
		private String @Nullable [] patterns;
		private @Nullable BiPredicate<String, ParseResult> inputValidator;
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
		public Builder<E,V> patterns(String... patterns) {
			this.patterns = patterns;
			return this;
		}

		@Override
		public Builder<E,V> inputValidator(BiPredicate<String, ParseResult> inputValidator) {
			this.inputValidator = inputValidator;
			return this;
		}

		@Override
		public Builder<E,V> eventValidator(Function<Class<?>, Validation> eventValidator) {
			this.eventValidator = eventValidator;
			return this;
		}

		@Override
		public Builder<E,V> getter(Converter<E, V> converter) {
			this.converter = converter;
			return this;
		}

		@Override
		public Builder<E,V> registerChanger(ChangeMode mode, Changer<E, V> changer) {
			changers.put(mode, changer);
			return this;
		}

		@Override
		public Builder<E,V> time(Time time) {
			this.time = time;
			return this;
		}

		@Override
		@SafeVarargs
		public final Builder<E, V> excludes(Class<? extends E>... events) {
			this.excludedEvents = events;
			return this;
		}

		@Override
		public Builder<E, V> excludedErrorMessage(String excludedErrorMessage) {
			this.excludedErrorMessage = excludedErrorMessage;
			return this;
		}

		@Override
		public EventValue<E, V> build() {
			if (patterns == null) {
				boolean plural = valueClass.isArray();
				//noinspection unchecked
				ClassInfo<?> type = Classes.getExactClassInfo(plural ? (Class<V>) valueClass.getComponentType() : valueClass);
				if (type != null && type.getUserInputPatterns() != null) {
					inputValidator = combinePredicates(
						(input, parseResult) -> plural == Utils.getEnglishPlural(input).getSecond(),
						inputValidator
					);
				}
			}

			return new EventValueImpl<>(
				eventClass,
				valueClass,
				patterns,
				inputValidator,
				eventValidator,
				converter,
				changers,
				time,
				excludedEvents,
				excludedErrorMessage
			);
		}

		private static <T, U> BiPredicate<T, U> combinePredicates(
			@Nullable BiPredicate<T, U> first,
			@Nullable BiPredicate<T, U> second
		) {
			if (first == null)
				return second;
			if (second == null)
				return first;
			return first.and(second);
		}

	}

}
