package ch.njol.skript.expressions.base;

import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.DefaultExpression;
import ch.njol.skript.lang.EffectSection;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionProvider;
import ch.njol.skript.lang.ExpressionSection;
import ch.njol.skript.lang.Section;
import ch.njol.skript.lang.SectionEvent;
import ch.njol.skript.lang.SectionSkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxElement;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.structure.Structure;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

/**
 * Typed {@link DefaultExpression} to grab values from {@link EffectSection}s and {@link SectionExpression}s.
 * <p>
 *     Similar to {@link EventValueExpression}, this class only works for the aforementioned classes in which they must
 *     use {@link SectionEvent} to load their section.
 *     This class allows utilization of {@link DefaultExpression}s for {@link ClassInfo}s without granting the
 *     ability to retrieve it through a Bukkit Event via conversion.
 * </p>
 */
public class SectionValueExpression<T> extends SimpleExpression<T> implements DefaultExpression<T> {

	private static final Map<Class<? extends SyntaxElement>, Class<?>[]> SECTION_VALUES = new HashMap<>();

	/**
	 * Register the types of classes {@code elementClass} can be returned as.
	 * <p>
	 *     Registering allows a {@link SectionValueExpression} to determine if it's available and successfully parse.
	 *     Not registering will use the {@link Class} from {@link Expression#getReturnType()}
	 * </p>
	 *
	 * @param elementClass The {@link SyntaxElement} class to register.
	 * @param valueClasses The types of {@link Class}es {@code elementClass} can be used with.
	 */
	public static void registerSectionValue(
		Class<? extends SyntaxElement> elementClass,
		Class<?>... valueClasses
	) {
		SECTION_VALUES.put(elementClass, valueClasses);
	}

	/**
	 * Create a new {@link Builder} to build a {@link SectionValueExpression}.
	 *
	 * @param type The typed {@link Class}.
	 * @return {@link Builder}.
	 */
	public static <T> Builder<T> builder(Class<? extends T> type) {
		return new Builder<>(type);
	}

	/**
	 * Constructs a simple {@link SectionValueExpression} with the provided {@code type}.
	 *
	 * @param type The typed {@link Class}.
	 * @return {@link SectionValueExpression}.
	 */
	public static <T> SectionValueExpression<T> simple(Class<? extends T> type) {
		//noinspection unchecked
		return (SectionValueExpression<T>) builder(type).build();
	}

	private final Class<? extends T> type;
	private final Class<?> componentType;
	private final boolean single;
	private @Nullable Changer<? super T> changer;

	private SectionValueExpression(Class<? extends T> type, @Nullable Changer<? super T> changer) {
		this.type = type;
		this.changer = changer;
		single = !type.isArray();
		componentType = single ? type : type.getComponentType();
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (expressions.length != 0)
			throw new SkriptAPIException(this.getClass().getName() + " has expressions in its pattern but does not override init(...)");
		return init();
	}

	@Override
	public boolean init() {
		ParserInstance parser = getParser();
		ParseLogHandler log = SkriptLogger.startParseLogHandler();
		try {
			boolean hasValue = false;
			Structure structure = parser.getCurrentStructure();
			if (!(structure instanceof SectionSkriptEvent sectionSkriptEvent))
				return false;

			Section section = sectionSkriptEvent.getSection();
			SyntaxElement syntaxElement;
			if (section instanceof ExpressionSection exprSec) {
				syntaxElement = exprSec.getAsExpression();
			} else {
				syntaxElement = section;
			}

			Expression<?> sectionExpression = null;
			if (syntaxElement instanceof ExpressionProvider provider) {
				sectionExpression = provider.getProvidedExpression();
			}

			Class<?>[] values = SECTION_VALUES.get(syntaxElement.getClass());
			if (values == null || values.length == 0) {
				if (!(syntaxElement instanceof Expression<?> expression))
					return false;
				values = new Class[]{expression.getReturnType()};
			}

			for (Class<?> value : values) {
				if (sectionExpression != null) {
					if (!sectionExpression.canReturn(componentType))
						continue;
				} else if (!value.equals(Object.class) && !componentType.isAssignableFrom(value)) {
					continue;
				}

				hasValue = true;
				break;
			}

			if (!hasValue) {
				log.printError("There's no " + Classes.getSuperClassInfo(componentType).getName().toString(!single) + " in " + Utils.a(sectionSkriptEvent.toString(null, false)) + " section");
				return false;
			}

			log.printLog();
			return true;
		} finally {
			log.stop();
		}
	}

	@Override
	protected T @Nullable [] get(Event event) {
		T value = getValue(event);
		if (value == null)
			//noinspection unchecked
			return (T[]) Array.newInstance(componentType, 0);
		if (single) {
			//noinspection unchecked
			T[] one = (T[]) Array.newInstance(type, 1);
			one[0] = value;
			return one;
		}
		//noinspection unchecked
		T[] dataArray = (T[]) value;
		//noinspection unchecked
		T[] array = (T[]) Array.newInstance(componentType, dataArray.length);
		System.arraycopy(dataArray, 0, array, 0, array.length);
		return array;
	}

	private @Nullable T getValue(Event event) {
		if (!(event instanceof SectionEvent<?> sectionEvent))
			return null;
		//noinspection unchecked
		return (T) sectionEvent.getObject();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return CollectionUtils.array(componentType);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (event instanceof SectionEvent<?> sectionEvent) {
			sectionEvent.change(event, delta, mode);
		}
	}

	@Override
	public boolean isSingle() {
		return single;
	}

	@Override
	public boolean isDefault() {
		return true;
	}

	@Override
	public Class<? extends T> getReturnType() {
		//noinspection unchecked
		return (Class<? extends T>) componentType;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (!debug || event == null)
			return Classes.getSuperClassInfo(componentType).getName().toString(!single);
		return Classes.getDebugMessage(getValue(event));
	}

	/**
	 * Builder class to build a {@link SectionValueExpression}.
	 */
	public static class Builder<T> {

		private final Class<? extends T> type;
		private Changer<? super T> changer = null;

		private Builder(Class<? extends T> type) {
			this.type = type;
		}

		/**
		 * Apply a custom {@link Changer} to be used with {@link Expression#acceptChange(ChangeMode)}
		 * and {@link Expression#change(Event, Object[], ChangeMode)}.
		 *
		 * @param changer The {@link Changer}.
		 * @return {@code this}.
		 */
		public Builder<T> changer(Changer<? super T> changer) {
			this.changer = changer;
			return this;
		}

		/**
		 * Finalizes this builder and builds a {@link SectionValueExpression}.
		 *
		 * @return {@link SectionValueExpression}.
		 */
		public SectionValueExpression<T> build() {
			return new SectionValueExpression<>(type, changer);
		}

	}

}
