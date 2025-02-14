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
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.localization.Noun;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.registrations.EventValues.EventContext;
import ch.njol.skript.registrations.EventValues.EventValueContext;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.Converter;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;
import org.skriptlang.skript.util.Priority;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

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
	 */
	@ApiStatus.Experimental
	public static <E extends EventValueExpression<T>, T> SyntaxInfo.Expression<E, T> register(SyntaxRegistry registry, Class<E> expressionClass, Class<T> returnType, String pattern) {
		SyntaxInfo.Expression<E, T> info = SyntaxInfo.Expression.builder(expressionClass, returnType)
			.priority(DEFAULT_PRIORITY)
			.addPattern("[the] " + pattern)
			.build();
		registry.register(SyntaxRegistry.EXPRESSION, info);
		return info;
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

	private final Map<Class<? extends Event>, Converter<?, ? extends T>> converters = new HashMap<>();

	private final Class<?> componentType;
	private final Class<? extends T> type;

	@Nullable
	private Changer<? super T> changer;
	private final boolean single;
	private final boolean exact;
	private final boolean original;

	public EventValueExpression(Class<? extends T> type) {
		this(type, null);
	}

	public EventValueExpression(boolean original, Class<? extends T> type) {
		this(original, type, null);
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

	public EventValueExpression(boolean original, Class<? extends T> type, @Nullable Changer<? super T> changer) {
		this(original, type, changer, false);
	}

	public EventValueExpression(Class<? extends T> type, @Nullable Changer<? super T> changer, boolean exact) {
		this(false, type, changer, exact);
	}

	public EventValueExpression(boolean original, Class<? extends T> type, @Nullable Changer<? super T> changer, boolean exact) {
		assert type != null;
		this.type = type;
		this.exact = exact;
		this.changer = changer;
		this.original = original;
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
		ParseLogHandler log = SkriptLogger.startParseLogHandler();
		try {
			boolean hasValue = false;
			Class<? extends Event>[] events = getParser().getCurrentEvents();
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
					ClassInfo<?> classInfo = Classes.getExactClassInfo(componentType);
					if (classInfo == null)
						throw new IllegalStateException("No classinfo for: " + componentType);
					Noun typeName = classInfo.getName();
					log.printError("There are multiple " + typeName.toString(true) + " in " + Utils.a(getParser().getCurrentEventName()) + " event. " +
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
				}
			}
			if (!hasValue) {
				log.printError("There's no " + Classes.getSuperClassInfo(componentType).getName().toString(!single) + " in " + Utils.a(getParser().getCurrentEventName()) + " event");
				return false;
			}
			log.printLog();
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
	private <E extends Event> T getValue(E event) {
		EventContext<E, T> eventContext = EventValues.getEventContext(event, getTime());
		//noinspection unchecked
		EventValueContext<E, T> valueContext = eventContext.getEventValueContext((Class<T>) type);
		if (valueContext == null || !valueContext.isSingleConverter())
			return null;
		if (original)
			return valueContext.getOriginalValue();
		return valueContext.getCurrentValue();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (changer == null)
			//noinspection unchecked
			changer = (Changer<? super T>) Classes.getSuperClassInfo(componentType).getChanger();
		return changer == null ? null : changer.acceptChange(mode);
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (changer == null)
			throw new SkriptAPIException("The changer cannot be null");
		ChangerUtils.change(changer, getArray(event), delta, mode);
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
