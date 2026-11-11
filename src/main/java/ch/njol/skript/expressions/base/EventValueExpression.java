package ch.njol.skript.expressions.base;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.Changer.ChangerUtils;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.DefaultExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;
import ch.njol.util.coll.CollectionUtils;
import com.google.common.base.Preconditions;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry.Resolution;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.util.Priority;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A useful class for creating default expressions. It simply returns the event value of the given type.
 * <p>
 * This class can be used as default expression with <code>new EventValueExpression&lt;T&gt;(T.class)</code> or extended to make it manually placeable in expressions with:
 *
 * <pre>
 * class MyExpression extends EventValueExpression&lt;SomeClass&gt; {
 * 	public MyExpression() {
 * 		super(SomeClass.class);
 * 	}
 * 	// ...
 * }
 * </pre>
 *
 * @see Classes#registerClass(ClassInfo)
 * @see ClassInfo#defaultExpression(DefaultExpression)
 * @see DefaultExpression
 */
public class EventValueExpression<T> extends SimpleExpression<T> implements DefaultExpression<T> {

	/**
	 * A priority for {@link EventValueExpression}s.
	 * They will be registered before {@link SyntaxInfo#COMBINED} expressions
	 *  but after {@link SyntaxInfo#SIMPLE} expressions.
	 */
	public static final Priority DEFAULT_PRIORITY = Priority.before(SyntaxInfo.COMBINED);

	private static final EventValueRegistry.Flags NO_CONVERSION_FLAGS = EventValueRegistry.Flags.DEFAULT
		.without(EventValueRegistry.Flag.ALLOW_CONVERSION);

	/**
	 * Creates a builder for a {@link SyntaxInfo} representing a {@link EventValueExpression} with the provided patterns.
	 * The info will use {@link #DEFAULT_PRIORITY} as its {@link SyntaxInfo#priority()}.
	 * This method will append '[the]' to the beginning of each patterns
	 * @param expressionClass The expression class to be represented by the info.
	 * @param returnType The class representing the expression's return type.
	 * @param patterns The patterns to match for creating this expression.
	 * @param <T> The return type.
	 * @param <E> The Expression type.
	 * @return The registered {@link SyntaxInfo}.
	 */
	public static <E extends EventValueExpression<T>, T> SyntaxInfo.Expression.Builder<? extends SyntaxInfo.Expression.Builder<?, E, T>, E, T> infoBuilder(
			Class<E> expressionClass, Class<T> returnType, String... patterns) {
		for (int i = 0; i < patterns.length; i++) {
			patterns[i] = "[the] " + patterns[i];
		}
		return SyntaxInfo.Expression.builder(expressionClass, returnType)
			.priority(DEFAULT_PRIORITY)
			.addPatterns(patterns);
	}

	/**
	 * Registers an expression as {@link ExpressionType#EVENT} with the provided pattern.
	 * This also adds '[the]' to the start of the pattern.
	 *
	 * @param expression The class that represents this EventValueExpression.
	 * @param type The return type of the expression.
	 * @param pattern The pattern for this syntax.
	 * @deprecated Register the standard way using {@link #infoBuilder(Class, Class, String...)}
	 *  to create a {@link SyntaxInfo}.
	 */
	@Deprecated(since = "2.14", forRemoval = true)
	public static <T> void register(Class<? extends EventValueExpression<T>> expression, Class<T> type, String pattern) {
		Skript.registerExpression(expression, type, ExpressionType.EVENT, "[the] " + pattern);
	}

	/**
	 * Registers an expression as {@link ExpressionType#EVENT} with the provided patterns.
	 * This also adds '[the]' to the start of all patterns.
	 *
	 * @param expression The class that represents this EventValueExpression.
	 * @param type The return type of the expression.
	 * @param patterns The patterns for this syntax.
	 * @deprecated Register the standard way using {@link #infoBuilder(Class, Class, String...)}
	 *  to create a {@link SyntaxInfo}.
	 */
	@Deprecated(since = "2.14", forRemoval = true)
	public static <T> void register(Class<? extends EventValueExpression<T>> expression, Class<T> type, String ... patterns) {
		for (int i = 0; i < patterns.length; i++) {
			if (!StringUtils.startsWithIgnoreCase(patterns[i], "[the] "))
				patterns[i] = "[the] " + patterns[i];
		}
		Skript.registerExpression(expression, type, ExpressionType.EVENT, patterns);
	}

	private final EventValueRegistry registry = Skript.instance().registry(EventValueRegistry.class);
	public final Set<Class<? extends Event>> events = new HashSet<>();

	private final @Nullable Class<?> componentType;
	private final @Nullable Class<? extends T> type;
	private final @Nullable String identifier;

	private @Nullable Changer<? super T> changer;
	private final Kleenean single;
	private final boolean exact;
	private boolean isDelayed;

	public EventValueExpression(Class<? extends T> type) {
		this(type, null);
	}

	/**
	 * Construct an event value expression.
	 *
	 * @param type The class that this event value represents.
	 * @param exact If false, the event value can be a subclass or a converted event value.
	 */
	public EventValueExpression(Class<? extends T> type, boolean exact) {
		this(type, null, exact);
	}

	public EventValueExpression(Class<? extends T> type, @Nullable Changer<? super T> changer) {
		this(type, changer, false);
	}

	public EventValueExpression(Class<? extends T> type, @Nullable Changer<? super T> changer, boolean exact) {
		this(Preconditions.checkNotNull(type, "type"), null, changer, exact);
	}

	public EventValueExpression(String identifier) {
		this(identifier, null);
	}

	public EventValueExpression(String identifier, @Nullable Changer<? super T> changer) {
		this(null, Preconditions.checkNotNull(identifier, "identifier"), changer, false);
	}

	@Contract("null, null, _, _ -> fail")
	public EventValueExpression(@Nullable Class<? extends T> type, @Nullable String identifier, @Nullable Changer<? super T> changer, boolean exact) {
		if (type == null && identifier == null)
			throw new IllegalArgumentException("Either type or identifier must be non-null");
		this.type = type;
		this.identifier = identifier;
		this.exact = exact;
		this.changer = changer;
		single = type != null ? Kleenean.get(!type.isArray()) : Kleenean.UNKNOWN;
		componentType = single.isTrue() || single.isUnknown() ? type : type.getComponentType();
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
		if (expressions.length != 0)
			throw new SkriptAPIException(this.getClass().getName() + " has expressions in its pattern but does not override init(...)");
		return init();
	}

	/**
	 * Resolves event values for a given event class with default flags and current time.
	 *
	 * @param eventClass The event class to resolve for.
	 * @param <E> The event type.
	 * @return The resolution result.
	 */
	private <E extends Event> Resolution<E, ? extends T> resolve(Class<E> eventClass) {
		return resolve(eventClass, EventValueRegistry.Flags.DEFAULT);
	}

	/**
	 * Resolves event values for a given event class with specified flags and current time.
	 *
	 * @param eventClass The event class to resolve for.
	 * @param flags The flags to use for resolution.
	 * @param <E> The event type.
	 * @return The resolution result.
	 */
	private <E extends Event> Resolution<E, ? extends T> resolve(Class<E> eventClass, EventValueRegistry.Flags flags) {
		return resolve(eventClass, EventValue.Time.of(getTime()), flags);
	}

	/**
	 * Resolves event values for a given event class and a specific time.
	 * This method disables fallback to the default time state.
	 *
	 * @param eventClass The event class to resolve for.
	 * @param time The time to resolve at.
	 * @param <E> The event type.
	 * @return The resolution result.
	 */
	private <E extends Event> Resolution<E, ? extends T> resolveForTime(Class<E> eventClass, EventValue.Time time) {
		return resolve(
			eventClass,
			time,
			EventValueRegistry.Flags.DEFAULT.without(EventValueRegistry.Flag.FALLBACK_TO_DEFAULT_TIME_STATE)
		);
	}

	/**
	 * The core resolution logic for event values.
	 * This method handles both identifier-based and type-based lookups.
	 *
	 * @param eventClass The event class to resolve for.
	 * @param time The time to resolve at.
	 * @param flags The flags to use for resolution.
	 * @param <E> The event type.
	 * @return The resolution result.
	 */
	private <E extends Event> Resolution<E, ? extends T> resolve(
		Class<E> eventClass,
		EventValue.Time time,
		EventValueRegistry.Flags flags
	) {
		if (identifier != null) {
			Resolution<E, ? extends T> resolution = registry.resolve(eventClass, identifier, time, flags);
			if (type == null)
				return resolution;
			return Resolution.of(resolution.all().stream()
				.map(eventValue -> eventValue.getConverted(eventClass, type))
				.filter(Objects::nonNull)
				.toList());
		}
		return exact
			? registry.resolveExact(eventClass, type, time)
			: registry.resolve(eventClass, type, time, flags);
	}

	/**
	 * Gets a string representation of this expression's input for error messages and {@link #toString(Event, boolean)}.
	 *
	 * @param plural Whether the name should be plural.
	 * @return The identifier if it exists, otherwise the name of the component type's super class info.
	 */
	private String input(boolean plural) {
		if (identifier != null)
			return identifier;
		assert componentType != null;
		return Classes.getSuperClassInfo(componentType).getName().toString(plural);
	}

	@Override
	public boolean init() {
		ParserInstance parser = getParser();
		isDelayed = parser.getHasDelayBefore().isTrue();
		ParseLogHandler log = SkriptLogger.startParseLogHandler();
		try {
			boolean hasValue = false;
			Class<? extends Event>[] events = parser.getCurrentEvents();
			if (events == null) {
				assert false;
				return false;
			}
			for (Class<? extends Event> event : events) {
				Resolution<?, ? extends T> resolution = resolve(event, NO_CONVERSION_FLAGS);
				if (resolution.multiple()) {
					log.printError("There are multiple " + input(true) + " in " + Utils.a(parser.getCurrentEventName()) + " event. " +
							"You must define which " + input(false) + " to use.");
					return false;
				}
				resolution = resolve(event);
				if (resolution.successful())
					hasValue = true;
			}
			if (!hasValue) {
				String message = null;

				if (type != null) {
					Class<?> suggested = type.isArray() ? componentType : type.arrayType();
					assert suggested != null;

					EventValueExpression<?> suggestedEventValue = new EventValueExpression<>(suggested);
					boolean suggestedValueExists = false;

					for (Class<? extends Event> event : events) {
						if (suggestedEventValue.resolve(event, NO_CONVERSION_FLAGS).multiple()
							|| !suggestedEventValue.resolve(event).successful())
							continue;
						suggestedValueExists = true;
						break;
					}

					if (suggestedValueExists) {
						if (suggested.isArray()) {
							message = "There are multiple " + suggestedEventValue.input(true);
						} else {
							message = "There's only one " + suggestedEventValue.input(false);
						}
						message += " in " + Utils.a(parser.getCurrentEventName())
							+ " event. Did you mean 'event-" + suggestedEventValue.input(suggested.isArray()) + "'?";
					}
				}

				if (message == null) {
					boolean single = isSingle();
					String is = single ? "'s" : " are";
					message = "There" + is + " no " + input(!single) + " in " + Utils.a(parser.getCurrentEventName())
						+ " event.";
				}

				log.printError(message);
				return false;
			}
			log.printLog();
			this.events.addAll(Arrays.asList(events));
			return true;
		} finally {
			log.stop();
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	protected T @Nullable [] get(Event event) {
		T value = getValue(event);
		if (value == null)
			return (T[]) Array.newInstance(getReturnType(), 0);
		if (isSingle()) {
			T[] one = (T[]) Array.newInstance(getReturnType(), 1);
			one[0] = value;
			return one;
		}
		T[] dataArray = (T[]) value;
		T[] array = (T[]) Array.newInstance(getReturnType(), dataArray.length);
		System.arraycopy(dataArray, 0, array, 0, array.length);
		return array;
	}

	@Nullable
	private <E extends Event> T getValue(E event) {
		Class<E> eventClass = getParseTimeEventClass(event);
		if (eventClass == null)
			return null;
		Resolution<E, ? extends T> resolution = resolve(eventClass);
		return resolution.anyOptional()
			.map(eventValue -> eventValue.get(event))
			.orElse(null);
	}

	private <E extends Event> Class<E> getParseTimeEventClass(E event) {
		for (Class<? extends Event> eventClass : events) {
			if (eventClass.isInstance(event)) {
				//noinspection unchecked
				return (Class<E>) eventClass;
			}
		}
		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		for (Class<? extends Event> event : events) {
			Resolution<?, ? extends T> resolution = resolve(event);
			if (!resolution.successful())
				continue;
			EventValue<?, ? extends T> found = resolution.all().stream()
				.filter(eventValue -> eventValue.hasChanger(mode))
				.findFirst().orElse(null);
			if (found == null)
				continue;
			if (isDelayed) {
				Skript.error("Event values cannot be changed after the event has already passed.");
				return null;
			}
			return CollectionUtils.array(found.valueClass());
		}

		if (changer == null)
			changer = (Changer<? super T>) Classes.getSuperClassInfo(getReturnType()).getChanger();
		return changer == null ? null : changer.acceptChange(mode);
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		Class<Event> eventClass = getParseTimeEventClass(event);
		if (eventClass == null)
			return;
		Resolution<?, ? extends T> resolution = resolve(eventClass);
		for (EventValue<?, ? extends T> eventValue : resolution.all()) {
			if (!eventValue.hasChanger(mode))
				continue;
			eventValue.changer(mode).ifPresent(changer -> {
				if (!eventValue.valueClass().isArray() && delta != null) {
					//noinspection unchecked,rawtypes
					((EventValue.Changer) changer).change(event, delta[0]);
				} else {
					//noinspection unchecked,rawtypes
					((EventValue.Changer) changer).change(event, delta);
				}
			});
			return;
		}

		if (changer != null) {
			ChangerUtils.change(changer, getArray(event), delta, mode);
		}
	}

	@Override
	public boolean setTime(int time) {
		Class<? extends Event>[] events = getParser().getCurrentEvents();
		if (events == null) {
			assert false;
			return false;
		}
		for (Class<? extends Event> event : events) {
			assert event != null;
			if (resolveForTime(event, EventValue.Time.PAST).successful()
				|| resolveForTime(event, EventValue.Time.FUTURE).successful()) {
				super.setTime(time);
				// Since the time was changed, we now need to re-initialize the parse time events we already got. START
				this.events.clear();
				init();
				// END
				return true;
			}
		}
		return false;
	}

	/**
	 * @return true
	 */
	@Override
	public boolean isDefault() {
		return true;
	}

	@Override
	public boolean isSingle() {
		if (!single.isUnknown())
			return single.isTrue();
		for (Class<? extends Event> event : events) {
			Resolution<?, ? extends T> resolution = resolve(event);
			if (!resolution.successful())
				continue;
			Class<? extends T> valueClass = resolution.any().valueClass();
			if (valueClass.isArray())
				return false;
		}
		return true;
	}

	@Override
	public Class<? extends T>[] possibleReturnTypes() {
		if (componentType != null)
			//noinspection unchecked
			return new Class[] {componentType};
		Set<Class<? extends T>> types = new HashSet<>();
		for (Class<? extends Event> eventClass : events) {
			Resolution<?, ? extends T> resolution = resolve(eventClass);
			if (!resolution.successful())
				continue;
			resolution.anyOptional().ifPresent(eventValue -> {
				Class<? extends T> type = eventValue.valueClass();
				//noinspection unchecked
				type = type.isArray() ? (Class<? extends T>) type.componentType() : type;
				types.add(type);
			});
		}
		//noinspection unchecked
		return types.toArray(new Class[0]);
	}

	@Override
	public Class<? extends T> getReturnType() {
		Class<? extends T>[] classes = possibleReturnTypes();
		if (classes.length == 1)
			return classes[0];
		return Utils.highestDenominator(Object.class, classes);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (!debug || event == null)
			return "event-" + input(!isSingle());
		return Classes.getDebugMessage(getValue(event));
	}

}
