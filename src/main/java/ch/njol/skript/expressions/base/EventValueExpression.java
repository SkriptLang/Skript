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
import ch.njol.skript.localization.Noun;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.EventConverter;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.Converter;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;
import org.skriptlang.skript.util.Priority;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

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
	@ApiStatus.Experimental
	public static final Priority DEFAULT_PRIORITY = Priority.before(SyntaxInfo.COMBINED);

	/**
	 * Registers an event value expression with the provided pattern.
	 * The syntax info will be forced to use the {@link #DEFAULT_PRIORITY} priority.
	 * This also adds '[the]' to the start of the pattern.
	 *
	 * @param registry The SyntaxRegistry to register with.
	 * @param expressionClass The EventValueExpression class being registered.
	 * @param returnType The class representing the expression's return type.
	 * @param pattern The pattern to match for creating this expression.
	 * @param <T> The return type.
	 * @param <E> The Expression type.
	 * @return The registered {@link SyntaxInfo}.
	 * @deprecated Use {@link #infoBuilder(Class, Class, String...)} to build a {@link SyntaxInfo}
	 *  and then register it using {@code registry} ({@link SyntaxRegistry#register(SyntaxRegistry.Key, SyntaxInfo)}).
	 */
	@ApiStatus.Experimental
	@Deprecated(since = "2.12", forRemoval = true)
	public static <E extends EventValueExpression<T>, T> SyntaxInfo.Expression<E, T> register(SyntaxRegistry registry, Class<E> expressionClass, Class<T> returnType, String pattern) {
		return register(registry, expressionClass, returnType, new String[]{pattern});
	}

	/**
	 * Registers an event value expression with the provided patterns.
	 * The syntax info will be forced to use the {@link #DEFAULT_PRIORITY} priority.
	 * This also adds '[the]' to the start of the patterns.
	 *
	 * @param registry The SyntaxRegistry to register with.
	 * @param expressionClass The EventValueExpression class being registered.
	 * @param returnType The class representing the expression's return type.
	 * @param patterns The patterns to match for creating this expression.
	 * @param <T> The return type.
	 * @param <E> The Expression type.
	 * @return The registered {@link SyntaxInfo}.
	 * @deprecated Use {@link #infoBuilder(Class, Class, String...)} to build a {@link SyntaxInfo}
	 *  and then register it using {@code registry} ({@link SyntaxRegistry#register(SyntaxRegistry.Key, SyntaxInfo)}).
	 */
	@ApiStatus.Experimental
	@Deprecated(since = "2.12", forRemoval = true)
	public static <E extends EventValueExpression<T>, T> DefaultSyntaxInfos.Expression<E, T> register(
		SyntaxRegistry registry,
		Class<E> expressionClass,
		Class<T> returnType,
		String ... patterns
	) {
		SyntaxInfo.Expression<E, T> info = infoBuilder(expressionClass, returnType, patterns).build();
		registry.register(SyntaxRegistry.EXPRESSION, info);
		return info;
	}

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
	@ApiStatus.Experimental
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
	 */
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
	 */
	public static <T> void register(Class<? extends EventValueExpression<T>> expression, Class<T> type, String ... patterns) {
		for (int i = 0; i < patterns.length; i++) {
			if (!StringUtils.startsWithIgnoreCase(patterns[i], "[the] "))
				patterns[i] = "[the] " + patterns[i];
		}
		Skript.registerExpression(expression, type, ExpressionType.EVENT, patterns);
	}

	private final Map<Class<? extends Event>, Converter<?, ? extends T>> converters = new HashMap<>();
	private final Map<Class<? extends Event>, EventConverter<Event, T>> eventConverters = new HashMap<>();

	private final Class<?> componentType;
	private final Class<? extends T> type;

	@Nullable
	private Changer<? super T> changer;
	private final boolean single;
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
		assert type != null;
		this.type = type;
		this.exact = exact;
		this.changer = changer;
		single = !type.isArray();
		componentType = single ? type : type.getComponentType();
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
		if (expressions.length != 0)
			throw new SkriptAPIException(this.getClass().getName() + " has expressions in its pattern but does not override init(...)");
		return init();
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
				if (converters.containsKey(event)) {
					hasValue = converters.get(event) != null;
					continue;
				}
				if (EventValues.hasMultipleConverters(event, type, getTime()) == Kleenean.TRUE) {
					Noun typeName = Classes.getExactClassInfo(componentType).getName();
					log.printError("There are multiple " + typeName.toString(true) + " in " + Utils.a(parser.getCurrentEventName()) + " event. " +
							"You must define which " + typeName + " to use.");
					return false;
				}
				Converter<?, ? extends T> converter;
				if (exact) {
					converter = EventValues.getExactEventValueConverter(event, type, getTime());
				} else {
					converter = EventValues.getEventValueConverter(event, type, getTime());
				}
				if (converter != null) {
					converters.put(event, converter);
					hasValue = true;
					if (converter instanceof EventConverter eventConverter) {
						eventConverters.put(event, eventConverter);
					}
				}
			}
			if (!hasValue) {
				log.printError("There's no " + Classes.getSuperClassInfo(componentType).getName().toString(!single) + " in " + Utils.a(parser.getCurrentEventName()) + " event");
				return false;
			}
			log.printLog();
			return true;
		} finally {
			log.stop();
		}
	}

	@Override
	@Nullable
	@SuppressWarnings("unchecked")
	protected T[] get(Event event) {
		T value = getValue(event);
		if (value == null)
			return (T[]) Array.newInstance(componentType, 0);
		if (single) {
			T[] one = (T[]) Array.newInstance(type, 1);
			one[0] = value;
			return one;
		}
		T[] dataArray = (T[]) value;
		T[] array = (T[]) Array.newInstance(componentType, dataArray.length);
		System.arraycopy(dataArray, 0, array, 0, array.length);
		return array;
	}

	@Nullable
	@SuppressWarnings("unchecked")
	private <E extends Event> T getValue(E event) {
		if (converters.containsKey(event.getClass())) {
			final Converter<? super E, ? extends T> g = (Converter<? super E, ? extends T>) converters.get(event.getClass());
			return g == null ? null : g.convert(event);
		}

		for (final Entry<Class<? extends Event>, Converter<?, ? extends T>> p : converters.entrySet()) {
			if (p.getKey().isAssignableFrom(event.getClass())) {
				converters.put(event.getClass(), p.getValue());
				return p.getValue() == null ? null : ((Converter<? super E, ? extends T>) p.getValue()).convert(event);
			}
		}

		converters.put(event.getClass(), null);

		return null;
	}

	@Override
	@Nullable
	@SuppressWarnings("unchecked")
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET && !eventConverters.isEmpty()) {
			if (isDelayed) {
				Skript.error("Event values cannot be changed after the event has already passed.");
				return null;
			}
			return CollectionUtils.array(type);
		}
		if (changer == null)
			changer = (Changer<? super T>) Classes.getSuperClassInfo(componentType).getChanger();
		return changer == null ? null : changer.acceptChange(mode);
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		if (mode == ChangeMode.SET) {
			EventConverter<Event, T> converter = eventConverters.get(event.getClass());
			if (converter != null) {
				if (!type.isArray() && delta != null) {
					converter.set(event, (T)delta[0]);
				} else {
					converter.set(event, (T)delta);
				}
				return;
			}
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
			boolean has;
			if (exact) {
				has = EventValues.doesExactEventValueHaveTimeStates(event, type);
			} else {
				has = EventValues.doesEventValueHaveTimeStates(event, type);
			}
			if (has) {
				super.setTime(time);
				// Since the time was changed, we now need to re-initialize the getters we already got. START
				converters.clear();
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
		return single;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Class<? extends T> getReturnType() {
		return (Class<? extends T>) componentType;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (!debug || event == null)
			return "event-" + Classes.getSuperClassInfo(componentType).getName().toString(!single);
		return Classes.getDebugMessage(getValue(event));
	}

}
