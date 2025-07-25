package ch.njol.skript.expressions.base;

import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.Changer.ChangerUtils;
import ch.njol.skript.lang.DefaultExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionSection;
import ch.njol.skript.lang.Section;
import ch.njol.skript.lang.SectionEvent;
import ch.njol.skript.lang.SectionSkriptEvent;
import ch.njol.skript.lang.SectionableExpression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxElement;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.sections.ExprSecBuildable;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.structure.Structure;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

public class SectionValueExpression<T> extends SimpleExpression<T> implements DefaultExpression<T> {

	private static final Map<Class<? extends SyntaxElement>, Class<?>[]> SECTION_VALUES =  new HashMap<>();

	public static void registerSectionValue(
		Class<? extends SyntaxElement> elementClass,
		Class<?>... valueClasses
	) {
		SECTION_VALUES.put(elementClass, valueClasses);
	}

	public static <T> Builder<T> builder(Class<? extends T> type) {
		return new Builder<>(type);
	}

	public static <T> SectionValueExpression<T> simple(Class<? extends T> type) {
		//noinspection unchecked
		return (SectionValueExpression<T>) builder(type).build();
	}

	private final Class<? extends T> type;
	private final Class<?> componentType;
	private final boolean exact;
	private final boolean single;
	private @Nullable Changer<? super T> changer;
	private @Nullable SectionableExpression<T> sectionableExpression = null;

	private SectionValueExpression(Class<? extends T> type, boolean exact, @Nullable Changer<? super T> changer) {
		this.type = type;
		this.exact = exact;
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
				SectionExpression<?> secExpr = exprSec.getAsExpression();
				syntaxElement = secExpr;
				if (secExpr instanceof ExprSecBuildable exprSecBuildable) {
					//noinspection unchecked
					sectionableExpression = (SectionableExpression<T>) exprSecBuildable.getSectionableExpression();
				}
			} else {
				syntaxElement = section;
			}

			Class<?>[] values = SECTION_VALUES.get(syntaxElement.getClass());
			if (values != null && values.length >= 1) {
				for (Class<?> value : values) {
					if (sectionableExpression != null) {
						if (!sectionableExpression.canReturn(componentType))
							continue;
					} else if (!value.equals(Object.class) && !componentType.isAssignableFrom(value)) {
						continue;
					}

					hasValue = true;
					break;
				}
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
		if (sectionableExpression != null)
			return sectionableExpression.getArray(event);
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
		if (sectionableExpression != null)
			return sectionableExpression.getSingle(event);
		//noinspection unchecked
		return (T) sectionEvent.getObject();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (sectionableExpression != null)
			return sectionableExpression.acceptChange(mode);
		if (changer == null) {
			//noinspection unchecked
			changer = (Changer<? super T>) Classes.getSuperClassInfo(componentType).getChanger();
		}
		return changer == null ? null : changer.acceptChange(mode);
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (sectionableExpression != null) {
			sectionableExpression.change(event, delta, mode);
			return;
		}
		if (changer == null)
			throw new SkriptAPIException("The changer can not be null");
		ChangerUtils.change(changer, getArray(event), delta, mode);
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

	public static class Builder<T> {

		private final Class<? extends T> type;
		private boolean exact = false;
		private Changer<? super T> changer = null;

		private Builder(Class<? extends T> type) {
			this.type = type;
		}

		public Builder<T> exact(boolean exact) {
			this.exact = exact;
			return this;
		}

		public Builder<T> changer(Changer<? super T> changer) {
			this.changer = changer;
			return this;
		}

		public SectionValueExpression<T> build() {
			return new SectionValueExpression<>(type, exact, changer);
		}

	}

}
